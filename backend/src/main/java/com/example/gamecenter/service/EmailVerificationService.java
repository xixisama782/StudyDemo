package com.example.gamecenter.service;

import com.example.gamecenter.constant.EmailVerificationPurpose;

/** 邮箱验证码发送与校验（Redis 优先，无 Redis 时内存降级）。 */
public interface EmailVerificationService {
    void sendCode(String email, EmailVerificationPurpose purpose);

    boolean verifyAndConsume(String email, EmailVerificationPurpose purpose, String code);
}
