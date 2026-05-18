package com.example.shop.web;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        String context = request.getContextPath();
        String path = uri.substring(context.length());
        if (path.startsWith("/assets/") || path.equals("/login") || path.equals("/logout") || path.equals("/error")) {
            return true;
        }
        if (request.getSession().getAttribute("loginUser") != null) {
            return true;
        }
        response.sendRedirect(context + "/login");
        return false;
    }
}
