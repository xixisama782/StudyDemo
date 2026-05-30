package com.example.gamecenter.constant;

/** REST API 路径常量，供 Controller 与前端路由对齐。 */
public final class ApiConstants {

    public static final class Auth {
        public static final String BASE = "/api/auth";
        public static final String REGISTER = "/register";
        public static final String SEND_CODE = "/send-code";
        public static final String LOGIN = "/login";
        public static final String LOGOUT = "/logout";
    }

    public static final class AdminAuth {
        public static final String BASE = "/api/admin/auth";
        public static final String LOGIN = "/login";
        public static final String LOGOUT = "/logout";
    }

    public static final class Users {
        public static final String BASE = "/api/users";
        public static final String ME = "/me";
        public static final String ME_PASSWORD = "/me/password";
        public static final String ME_PASSWORD_SEND_CODE = "/me/password/send-code";
        public static final String ME_AVATAR = "/me/avatar";
        /** 未设置头像时 API 返回的默认展示地址（静态资源，不落库） */
        public static final String DEFAULT_AVATAR_URL =
                "/uploads/avatars/3e9ce3813b7199ea9588eeb920f41208_512_512.jpg";
    }

    public static final class UserStats {
        public static final String BASE = "/api/users/me";
        public static final String STATISTICS = "/statistics";
        public static final String LEADERBOARDS = "/leaderboards";
    }

    public static final class Games {
        public static final String BASE = "/api/games";
        public static final String ADMIN_LIST = "/admin/list";
        public static final String LEADERBOARD = "/{id}/leaderboard";
        public static final String LEADERBOARD_ME = "/{id}/leaderboard/me";
        public static final String PLAY = "/{id}/play";
    }

    public static final class GameTypes {
        public static final String BASE = "/api/game-types";
    }

    /** 管理端：游戏 CRUD，与公开列表 {@link Games#BASE} 分离 */
    public static final class AdminGames {
        public static final String BASE = "/api/admin/games";
    }

    /** 管理端：游戏类型 CRUD */
    public static final class AdminGameTypes {
        public static final String BASE = "/api/admin/game-types";
    }

    /** 管理端：用户列表与状态 */
    public static final class AdminUsers {
        public static final String BASE = "/api/admin/users";
    }

    /** 管理端：统计概览与热门游戏 */
    public static final class AdminStatistics {
        public static final String BASE = "/api/admin/statistics";
        public static final String OVERVIEW = "/overview";
        public static final String POPULAR_GAMES = "/popular-games";
    }

    public static final class PlayHistory {
        public static final String BASE = "/api/users/me/history";
    }

    public static final class GameSession {
        public static final String BASE = "/api/users/me/history/session";
        public static final String START = "/start";
        public static final String END = "/{sessionId}/end";
        public static final String CURRENT = "/current";
        /** sendBeacon 兜底结束会话（请求体含 token，供无 Authorization 头场景） */
        public static final String BEACON_END = "/beacon/end";
    }

    public static final class Favorites {
        public static final String BASE = "/api/users/me/favorites";
        public static final String CHECK = "/{gameId}/check";
        /** POST body: { "gameIds": number[] } → data.favoritedGameIds */
        public static final String BATCH_CHECK = "/batch-check";
    }

    private ApiConstants() {
    }
}
