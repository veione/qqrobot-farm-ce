package de.honoka.qqrobot.farm.web;

import de.honoka.qqrobot.farm.web.interceptor.BaseInterceptor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import java.util.List;

@Configuration
public class InterceptorRegister implements WebMvcConfigurer {

    @Override
    public void addInterceptors(@NotNull InterceptorRegistry registry) {
        //自定义拦截器
        for(BaseInterceptor interceptor : interceptors) {
            registry.addInterceptor(interceptor)
                    .addPathPatterns(interceptor.pathPatterns)
                    .excludePathPatterns(interceptor.excludePathPatterns);
        }
    }

    @Resource
    private List<BaseInterceptor> interceptors;
}
