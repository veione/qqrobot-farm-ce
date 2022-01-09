package de.honoka.qqrobot.farm.web.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

public abstract class BaseInterceptor implements HandlerInterceptor {

    public List<String> pathPatterns, excludePathPatterns;
}
