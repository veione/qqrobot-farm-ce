package de.honoka.qqrobot.farm.web.interceptor;

import de.honoka.qqrobot.farm.system.ExtendRobotAttributes;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;

@Component
public class LoginInterceptor extends BaseInterceptor {

    public LoginInterceptor() {
        pathPatterns = Collections.singletonList("/**");
        excludePathPatterns = Arrays.asList(
                "/static/**", "/login", "/api/vue-admin/login", "/farm/**",
                "/text", "/changeLocation", "/register", "/robot/static/**",
                "/", "/favicon.ico", "/index.html"
        );
    }

    /**
     * 在处理方法之前执行，一般用来做一些准备工作：比如日志，权限检查
     * 如果返回false，表示被拦截，将不会执行处理方法
     * 返回true，继续执行处理方法
     */
    @Override
    @SneakyThrows
    public boolean preHandle(@NotNull HttpServletRequest request,
                             @NotNull HttpServletResponse response,
                             @NotNull Object handler) {
        if(request.getMethod().toLowerCase(Locale.ROOT).equals("options"))
            return true;
        String token = request.getHeader("X-Token");
        if(!Objects.equals(token, ExtendRobotAttributes.WEB_LOGIN_PASSWORD)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().println("请登录");
            return false;
        }
        return true;
    }

    /**
     * 在处理方法执行之后，在渲染视图执行之前执行，一般用来做一些清理工作
     */
    @Override
    public void postHandle(@NotNull HttpServletRequest request,
                           @NotNull HttpServletResponse response,
                           @NotNull Object handler, ModelAndView modelAndView) {

    }

    /**
     * 在视图渲染后执行，一般用来释放资源
     */
    @Override
    public void afterCompletion(@NotNull HttpServletRequest request,
                                @NotNull HttpServletResponse response,
                                @NotNull Object handler, Exception ex) {

    }
}
