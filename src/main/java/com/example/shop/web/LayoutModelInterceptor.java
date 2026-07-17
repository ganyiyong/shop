package com.example.shop.web;

import com.example.shop.model.DashboardStats;
import com.example.shop.repository.StatsRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
public class LayoutModelInterceptor implements HandlerInterceptor {
    private final StatsRepository statsRepository;

    public LayoutModelInterceptor(StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           ModelAndView modelAndView) {
        if (modelAndView == null || !"GET".equalsIgnoreCase(request.getMethod())) {
            return;
        }
        String viewName = modelAndView.getViewName();
        if (viewName == null || viewName.startsWith("redirect:") || "login".equals(viewName)) {
            return;
        }
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loginUser") == null) {
            return;
        }

        Object dashboardStats = modelAndView.getModel().get("stats");
        double monthSaleAmount = dashboardStats instanceof DashboardStats
                ? ((DashboardStats) dashboardStats).getMonthSaleAmount()
                : statsRepository.currentMonthSaleAmount();
        modelAndView.addObject("layoutMonthSaleAmount", monthSaleAmount);
    }
}
