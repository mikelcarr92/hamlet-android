package com.twopicode.hamlet.Util;

import java.util.Map;

/****************************************
 * Allows transfer of cookies from WebView to Volley.
 *
 * Created by michaelcarr on 28/11/15.
 ****************************************/
public class CookieMonster {

    private static final String SET_COOKIE_KEY = "Set-Cookie";
    private static final String COOKIE_KEY = "Cookie";
    private static final String SESSION_COOKIE = "sessionid";

    public static String cookies = "";

    public static void setCookieFromWebView(String cookie) {
        cookies = cookie;
    }

    /**
     * Checks the response headers for session cookie and saves it
     * if it finds it.
     * @param headers Response Headers.
     */
    public static void checkSessionCookie(Map<String, String> headers) {

        if (headers.containsKey(SET_COOKIE_KEY) && headers.get(SET_COOKIE_KEY).startsWith(SESSION_COOKIE)) {
            String cookie = headers.get(SET_COOKIE_KEY);
            if (cookie.length() > 0) {
                String[] splitCookie = cookie.split(";");
                String[] splitSessionId = splitCookie[0].split("=");
                cookie = splitSessionId[1];
                cookies = cookie;
            }
        }
    }

    /**
     * Adds session cookie to headers if exists.
     * @param headers
     */
    public static void addSessionCookie(Map<String, String> headers) {
        String sessionId = cookies;
        if (sessionId.length() > 0) {
            StringBuilder builder = new StringBuilder();
            builder.append(SESSION_COOKIE);
            builder.append("=");
            builder.append(sessionId);
            if (headers.containsKey(COOKIE_KEY)) {
                builder.append("; ");
                builder.append(headers.get(COOKIE_KEY));
            }
            headers.put(COOKIE_KEY, builder.toString());
        }
    }
}
