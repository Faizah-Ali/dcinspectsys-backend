package com.dhc.inspection_system.utils;

import jakarta.servlet.http.HttpServletRequest;

import java.net.InetAddress;

/** Client IP resolution matching legacy MyUtil.getClientIp. */
public final class ClientIpHelper {

    private ClientIpHelper() {
    }

    public static String getClientIp(HttpServletRequest request) {
        String remoteAddr = "";

        if (request == null) {
            return remoteAddr;
        }

        try {
            remoteAddr = request.getHeader("X-Forwarded-For");
            if (remoteAddr == null || remoteAddr.isEmpty() || "unknown".equalsIgnoreCase(remoteAddr)) {
                remoteAddr = request.getHeader("Proxy-Client-IP");
            }
            if (remoteAddr == null || remoteAddr.isEmpty() || "unknown".equalsIgnoreCase(remoteAddr)) {
                remoteAddr = request.getHeader("WL-Proxy-Client-IP");
            }
            if (remoteAddr == null || remoteAddr.isEmpty() || "unknown".equalsIgnoreCase(remoteAddr)) {
                remoteAddr = request.getHeader("HTTP_CLIENT_IP");
            }
            if (remoteAddr == null || remoteAddr.isEmpty() || "unknown".equalsIgnoreCase(remoteAddr)) {
                remoteAddr = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (remoteAddr == null || remoteAddr.isEmpty() || "unknown".equalsIgnoreCase(remoteAddr)) {
                remoteAddr = request.getRemoteAddr();
            }
            if (remoteAddr != null && remoteAddr.equalsIgnoreCase("0:0:0:0:0:0:0:1")) {
                InetAddress inetAddress = InetAddress.getLocalHost();
                remoteAddr = inetAddress.getHostAddress();
            }
        } catch (Exception ex) {
            // Keep empty / last known value; callers treat audit IP as best-effort.
        }

        return remoteAddr == null ? "" : remoteAddr;
    }
}
