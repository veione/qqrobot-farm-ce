package de.honoka.qqrobot.farm.web.controller;

import de.honoka.qqrobot.farm.service.RegisterService;
import de.honoka.qqrobot.farm.service.UserService;
import de.honoka.qqrobot.spring.boot.starter.component.ExceptionReporter;
import de.honoka.util.various.WebUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
public class UserController {

    @RequestMapping("/changeLocation")
    public String changeLocation(
            @RequestParam String id, HttpServletRequest httpRequest) {
        try {
            return userService.changeLocation(id, WebUtils.getRealIp(httpRequest));
        } catch(Exception e) {
            reporter.sendExceptionToDevelopingGroup(e);
            return "请求更新地理位置时出现错误，已向开发者报告错误信息";
        }
    }

    @RequestMapping("/register")
    public String register(@RequestParam String id, HttpServletRequest httpRequest) {
        try {
            return registerService.register(id, WebUtils.getRealIp(httpRequest));
        } catch(Exception e) {
            reporter.sendExceptionToDevelopingGroup(e);
            return "请求注册时出现错误，已向开发者报告错误信息";
        }
    }

    @Resource
    private UserService userService;

    @Resource
    private ExceptionReporter reporter;

    @Resource
    private RegisterService registerService;
}
