package com.example.gamecenter.constant;

/**
 * 业务层统一错误码与文案（可随模块渐进扩充）。
 * 与 {@link com.example.gamecenter.utils.Result} 及 Api 规范中的 HTTP 语义码对齐。
 */
public enum ApiBizError {

    // —— 游戏类型
    GAME_TYPE_CODE_EXISTS(409, "游戏类型代码已存在"),
    GAME_TYPE_NOT_FOUND(404, "游戏类型不存在"),
    GAME_TYPE_HAS_GAMES(400, "该类型下存在游戏，无法删除"),

    // —— 游戏
    GAME_NOT_FOUND(404, "游戏不存在"),
    GAME_NAME_TYPE_REQUIRED(400, "游戏名称和类型为必填字段"),

    // —— 收藏
    FAVORITE_ALREADY_EXISTS(400, "已收藏该游戏"),
    FAVORITE_NOT_FOUND(400, "未收藏该游戏"),

    // —— 游戏会话
    SESSION_NOT_FOUND(404, "会话不存在"),
    SESSION_ACCESS_DENIED(403, "无权操作该会话"),
    SESSION_ALREADY_ENDED(400, "会话已结束"),

    // —— 用户 / 管理端用户
    USER_NOT_FOUND(404, "用户不存在"),
    ADMIN_STATUS_REQUIRED(400, "status 为必填"),
    ADMIN_STATUS_INVALID(400, "status 须为 normal 或 disabled"),
    ADMIN_NEW_PASSWORD_REQUIRED(400, "newPassword 为必填"),
    ADMIN_NEW_PASSWORD_TOO_SHORT(400, "newPassword 至少 6 位"),

    // —— 认证（注册/登录）
    AUTH_USERNAME_CONFLICT(409, "用户名已存在"),
    AUTH_EMAIL_CONFLICT(409, "该邮箱已被注册"),
    AUTH_BAD_CREDENTIALS(401, "用户名或密码错误"),
    AUTH_UNAUTHORIZED(401, "未登录或无权访问"),
    AUTH_EMAIL_REQUIRED(400, "注册须填写邮箱"),
    AUTH_VERIFICATION_CODE_REQUIRED(400, "请填写邮箱验证码"),
    AUTH_VERIFICATION_CODE_INVALID(400, "邮箱验证码错误或已过期"),
    AUTH_EMAIL_PURPOSE_INVALID(400, "验证码用途无效"),
    AUTH_EMAIL_NOT_REGISTERED(400, "该邮箱未注册"),

    // —— 邮箱验证码
    EMAIL_NOT_CONFIGURED(500, "邮件服务未配置，请联系管理员"),
    EMAIL_SEND_FAILED(500, "验证码邮件发送失败，请稍后重试"),
    EMAIL_CODE_SEND_TOO_FREQUENT(400, "发送过于频繁，请稍后再试"),
    USER_EMAIL_NOT_BOUND(400, "账户未绑定邮箱，无法发送验证码"),

    // —— 用户端资料 / 密码 / 头像
    USER_PASSWORD_BOTH_REQUIRED(400, "请同时填写原密码与新密码"),
    USER_PASSWORD_CODE_OR_OLD_REQUIRED(400, "请填写邮箱验证码或原密码"),
    USER_OLD_PASSWORD_WRONG(400, "原密码错误"),
    USER_AVATAR_FILE_REQUIRED(400, "请选择头像文件"),
    USER_AVATAR_UNSUPPORTED_TYPE(400, "不支持的头像文件类型"),
    USER_AVATAR_PROCESS_FAILED(500, "头像处理失败，请稍后重试"),

    // —— 通用：访问控制 / 参数（多 Controller 共用）；ACCESS_FORBIDDEN 供 Spring Security AccessDeniedHandler 等使用
    ACCESS_FORBIDDEN(403, "无权访问"),
    ADMIN_ACCESS_REQUIRED(403, "需要管理员权限"),
    PARAM_GAME_ID_REQUIRED(400, "参数 gameId 为必填"),
    PARAM_SESSION_ID_REQUIRED(400, "参数 sessionId 为必填"),
    PARAM_SCORE_REQUIRED(400, "参数 score 为必填"),
    REQUEST_BODY_INVALID(400, "请求体无效"),

    // —— 管理端登录
    ADMIN_LOGIN_FIELDS_REQUIRED(400, "请填写用户名和密码"),

    // —— 管理端创建游戏类型（Controller 层校验）
    GAME_TYPE_NAME_AND_CODE_REQUIRED(400, "名称和代码为必填字段");

    private final int code;
    private final String message;

    ApiBizError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
