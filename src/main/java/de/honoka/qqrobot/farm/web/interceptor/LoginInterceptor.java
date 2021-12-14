package de.honoka.qqrobot.farm.web.interceptor;

import de.honoka.qqrobot.farm.system.ExtendRobotAttributes;
import de.honoka.qqrobot.farm.web.WebConfigurations;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    public final List<String>
            pathPatterns = Collections.singletonList("/**"),
            excludePathPatterns = Arrays.asList(
                    "/static/**", "/login", "/api/checkLogin", "/farm/**",
                    "/text", "/changeLocation", "/register");

    public boolean checkLoginStatus(HttpSession session) {
        String uuid = (String) session.getAttribute("loginUUID");
        return uuid != null && uuid.equals(ExtendRobotAttributes.WEB_LOGIN_PASSWORD);
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
        HttpSession session = request.getSession();
        boolean status = checkLoginStatus(session);
        if(!status) {
            response.sendRedirect(webConf.getContextPath() + "/login");
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

    @Resource
    private WebConfigurations webConf;
}
