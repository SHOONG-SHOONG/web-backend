package shoong.web_backend.config;

public class WhiteList {
    public static final String[] WHITELIST = {
            "/login", "/join", "/logout", "/oauth2-jwt-header",
            "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**",
            "/live/main", "/live/scheduled", "/live/brand/live-onGoing",
            "/live/stream-key/search", "/live/stream-key/latest",
            "/item/search",
            "/item/summary/**", // <-- 여기에 슬래시 추가됨
            "/brand/list",
            "/brand/summary/**", // <-- 여기에 슬래시 추가됨
            "/live/vods",
            "/live/list"
    };
}