package com.footballacademy.util;

import jakarta.servlet.http.HttpServletRequest;

public final
class MediaUrlUtil {
    private MediaUrlUtil() {
    }
    public static String baseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();
        String ctx = request.getContextPath();
        boolean defaultPort =("http" .equalsIgnoreCase(scheme) && port == 80) ||("https" .equalsIgnoreCase(scheme) && port == 443);
        return scheme + "://" + host +(defaultPort ? "" : ":" + port) + ctx;
    }
    public static String toAbsolute(HttpServletRequest request, String path) {
        if (path == null || path.isBlank()) return null;
        if (path.startsWith("http://") || path.startsWith("https://")) return path;
        String p = path.startsWith("/") ? path : "/" + path;
        return baseUrl(request) + p;
    }
}
