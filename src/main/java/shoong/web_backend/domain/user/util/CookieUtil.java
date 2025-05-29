package shoong.web_backend.domain.user.util;

import jakarta.servlet.http.Cookie;

public class CookieUtil {
    public static Cookie createCookie(String key, String value, Integer expiredS) {
        Cookie cookie = new Cookie(key, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setAttribute("SameSite","None");
        cookie.setPath("/");
        cookie.setMaxAge(expiredS);
        return cookie;
    }
}