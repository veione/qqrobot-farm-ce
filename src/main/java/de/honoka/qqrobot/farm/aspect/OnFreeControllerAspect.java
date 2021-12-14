package de.honoka.qqrobot.farm.aspect;

import de.honoka.qqrobot.farm.common.StaticMessages;
import de.honoka.qqrobot.farm.common.UserStatus;
import de.honoka.qqrobot.farm.database.dao.UserDao;
import de.honoka.qqrobot.farm.entity.farm.User;
import de.honoka.qqrobot.farm.service.UserService;
import de.honoka.qqrobot.spring.boot.starter.command.CommandMethodArgs;
import de.honoka.util.various.AspectUtils;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Aspect
@Component
public class OnFreeControllerAspect {

    @SneakyThrows
    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) {
        Map<String, Object> params = AspectUtils.getNameAndValue(joinPoint);
        CommandMethodArgs args = (CommandMethodArgs) params.get("args");
        User u = userDao.selectById(args.getQq());
        if(u == null) return StaticMessages.notRegistered;
        String status = userService.getStatus(u);
        if(!status.equals(UserStatus.FREE))
            return "你当前正在" + status + "，请等待执行完成后再执行此操作";
        return joinPoint.proceed(joinPoint.getArgs());
    }

    //增强该类中所有被Command注解的方法
    @Pointcut("execution(* de.honoka.qqrobot.farm.controller.OnFreeController" +
            ".*(..)) && @annotation(de.honoka.qqrobot.spring.boot.starter" +
            ".annotation.Command)")
    public void pointcut() {
    }

    @Resource
    private UserService userService;

    @Resource
    private UserDao userDao;
}
