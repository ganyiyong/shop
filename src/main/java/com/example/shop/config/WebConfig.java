package com.example.shop.config;

import com.example.shop.web.AuthInterceptor;
import com.example.shop.web.LayoutModelInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AuthInterceptor authInterceptor;
    private final LayoutModelInterceptor layoutModelInterceptor;

    public WebConfig(AuthInterceptor authInterceptor, LayoutModelInterceptor layoutModelInterceptor) {
        this.authInterceptor = authInterceptor;
        this.layoutModelInterceptor = layoutModelInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor).addPathPatterns("/**");
        registry.addInterceptor(layoutModelInterceptor).addPathPatterns("/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/assets/**").addResourceLocations("classpath:/static/assets/");
    }
}
