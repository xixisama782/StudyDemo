package com.example.gamecenter.constant;

/** 邮箱验证码业务用途，与 Redis 键前缀及发信文案对应。 */
public enum EmailVerificationPurpose {
    REGISTER("register"),
    CHANGE_PASSWORD("change_password");

    private final String value;

    EmailVerificationPurpose(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static EmailVerificationPurpose fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toLowerCase();
        for (EmailVerificationPurpose purpose : values()) {
            if (purpose.value.equals(normalized)) {
                return purpose;
            }
        }
        return null;
    }
}
