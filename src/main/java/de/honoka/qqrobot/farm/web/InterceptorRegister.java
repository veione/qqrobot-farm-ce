package de.honoka.qqrobot.farm.web;

import de.honoka.qqrobot.farm.web.interceptor.LoginInterceptor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class InterceptorRegister implements WebMvcConfigurer {

    @Override
    public void addInterceptors(@NotNull InterceptorRegistry registry) {
        //自定义拦截器
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns(loginInterceptor.pathPatterns)
                .excludePathPatterns(loginInterceptor.excludePathPatterns);
    }

    @Resource
    private LoginInterceptor loginInterceptor;
}
