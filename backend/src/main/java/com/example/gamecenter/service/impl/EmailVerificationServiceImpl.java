package com.example.gamecenter.service.impl;

import com.example.gamecenter.constant.ApiBizError;
import com.example.gamecenter.constant.EmailVerificationPurpose;
import com.example.gamecenter.exception.BusinessException;
import com.example.gamecenter.service.EmailVerificationService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/** 邮箱验证码：限频、存储、异步发信与一次性校验消费。 */
@Service
public class EmailVerificationServiceImpl implements EmailVerificationService {
    private static final Logger log = LoggerFactory.getLogger(EmailVerificationServiceImpl.class);

    private static final String KEY_PREFIX = "gamecenter:email-code:";
    private static final String RATE_PREFIX = "gamecenter:email-code-rate:";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;
    private final String mailFrom;
    private final long codeExpireMinutes;
    private final long sendIntervalSeconds;

    private final Map<String, CodeEntry> memoryCodes = new ConcurrentHashMap<>();
    private final Map<String, Long> memoryRateLimits = new ConcurrentHashMap<>();

    public EmailVerificationServiceImpl(
            JavaMailSender mailSender,
            @Value("${app.email.from:}") String mailFrom,
            @Value("${app.email.code-expire-minutes:5}") long codeExpireMinutes,
            @Value("${app.email.send-interval-seconds:60}") long sendIntervalSeconds,
            @Autowired(required = false) StringRedisTemplate redisTemplate) {
        this.mailSender = mailSender;
        this.mailFrom = mailFrom;
        this.codeExpireMinutes = codeExpireMinutes;
        this.sendIntervalSeconds = sendIntervalSeconds;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void sendCode(String email, EmailVerificationPurpose purpose) {
        String normalizedEmail = normalizeEmail(email);
        assertMailConfigured();
        checkSendRate(normalizedEmail, purpose);

        String code = generateCode();
        storeCode(normalizedEmail, purpose, code);
        markSent(normalizedEmail, purpose);

        String subject = purpose == EmailVerificationPurpose.REGISTER
                ? "【游戏中心】注册验证码"
                : "【游戏中心】修改密码验证码";
        String body = """
                您好，

                您的验证码为：%s
                有效期 %d 分钟，请勿泄露给他人。

                如非本人操作，请忽略此邮件。
                """.formatted(code, codeExpireMinutes);

        CompletableFuture.runAsync(() -> deliverVerificationEmail(normalizedEmail, subject, body));
    }

    private void deliverVerificationEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);
            mailSender.send(message);
        } catch (MessagingException ex) {
            log.error("Failed to send verification email to {}", to, ex);
        }
    }

    /** 校验通过后删除验证码与限频键，防止重复使用。 */
    @Override
    public boolean verifyAndConsume(String email, EmailVerificationPurpose purpose, String code) {        if (code == null || code.isBlank()) {
            return false;
        }
        String normalizedEmail = normalizeEmail(email);
        String key = codeKey(normalizedEmail, purpose);
        String expected = getStoredCode(key);
        if (expected == null || !expected.equals(code.trim())) {
            return false;
        }
        deleteCode(key);
        deleteRateLimit(normalizedEmail, purpose);
        return true;
    }

    private void assertMailConfigured() {
        if (mailFrom == null || mailFrom.isBlank()) {
            throw new BusinessException(ApiBizError.EMAIL_NOT_CONFIGURED);
        }
    }

    private void checkSendRate(String email, EmailVerificationPurpose purpose) {
        String rateKey = rateKey(email, purpose);
        if (redisTemplate != null) {
            Boolean exists = redisTemplate.hasKey(rateKey);
            if (Boolean.TRUE.equals(exists)) {
                throw new BusinessException(ApiBizError.EMAIL_CODE_SEND_TOO_FREQUENT);
            }
            return;
        }
        Long until = memoryRateLimits.get(rateKey);
        if (until != null && until > System.currentTimeMillis()) {
            throw new BusinessException(ApiBizError.EMAIL_CODE_SEND_TOO_FREQUENT);
        }
    }

    private void markSent(String email, EmailVerificationPurpose purpose) {
        String rateKey = rateKey(email, purpose);
        Duration interval = Duration.ofSeconds(sendIntervalSeconds);
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(rateKey, "1", interval);
            return;
        }
        memoryRateLimits.put(rateKey, System.currentTimeMillis() + interval.toMillis());
    }

    private void storeCode(String email, EmailVerificationPurpose purpose, String code) {
        String key = codeKey(email, purpose);
        Duration ttl = Duration.ofMinutes(codeExpireMinutes);
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(key, code, ttl);
            return;
        }
        memoryCodes.put(key, new CodeEntry(code, System.currentTimeMillis() + ttl.toMillis()));
    }

    private String getStoredCode(String key) {
        if (redisTemplate != null) {
            return redisTemplate.opsForValue().get(key);
        }
        CodeEntry entry = memoryCodes.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.expiresAt < System.currentTimeMillis()) {
            memoryCodes.remove(key);
            return null;
        }
        return entry.code;
    }

    private void deleteCode(String key) {
        if (redisTemplate != null) {
            redisTemplate.delete(key);
            return;
        }
        memoryCodes.remove(key);
    }

    private void deleteRateLimit(String email, EmailVerificationPurpose purpose) {
        String rateKey = rateKey(email, purpose);
        if (redisTemplate != null) {
            redisTemplate.delete(rateKey);
            return;
        }
        memoryRateLimits.remove(rateKey);
    }

    private String generateCode() {
        int value = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(value);
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private static String codeKey(String email, EmailVerificationPurpose purpose) {
        return KEY_PREFIX + purpose.getValue() + ":" + email;
    }

    private static String rateKey(String email, EmailVerificationPurpose purpose) {
        return RATE_PREFIX + purpose.getValue() + ":" + email;
    }

    private record CodeEntry(String code, long expiresAt) {
    }
}
