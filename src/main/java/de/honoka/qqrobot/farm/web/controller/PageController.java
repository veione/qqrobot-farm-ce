package de.honoka.qqrobot.farm.web.controller;

import de.honoka.qqrobot.farm.service.SystemService;
import de.honoka.qqrobot.farm.system.ExtendRobotAttributes;
import de.honoka.qqrobot.farm.web.WebConfigurations;
import de.honoka.qqrobot.farm.web.interceptor.LoginInterceptor;
import de.honoka.standard.system.SystemInfoBean;
import de.honoka.util.text.TextUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

@Controller
public class PageController {

    @RequestMapping("/login")
    public ModelAndView login(HttpSession session) {
        //判断登录状态
        boolean status = loginInterceptor.checkLoginStatus(session);
        if(status) return new ModelAndView("redirect:/");
        return new ModelAndView("login", "webConf", webConf);
    }

    @RequestMapping("/text")
    public ModelAndView text(@RequestParam String text) {
        return new ModelAndView("text", "text",
                text.replace("\n", "<br>"));
    }

    @RequestMapping("/exception")
    public ModelAndView exception() {
        return new ModelAndView("exception", "webConf", webConf);
    }

    @RequestMapping("/usageLog")
    public ModelAndView usageLog(
            @RequestParam(required = false, defaultValue = "1") int page) {
        ModelAndView mav = new ModelAndView("usageLog");
        mav.addObject("page", page);
        mav.addObject("webConf", webConf);
        return mav;
    }

    @RequestMapping("/console")
    public ModelAndView console() {
        return new ModelAndView("console", "webConf", webConf);
    }

    @RequestMapping("/")
    public ModelAndView root() {
        return new ModelAndView("main")
                .addObject("systemInfo", new SystemInfoBean())
                .addObject("dataSource",
                        systemService.getDataSourceStatus())
                .addObject("willSendTestMessageOnRelogin",
                        TextUtils.boolSwitchToString(ExtendRobotAttributes
                                .willSendTestMessageOnRelogin))
                .addObject("willResendOnSendFailed",
                        TextUtils.boolSwitchToString(ExtendRobotAttributes
                                .willResendOnSendFailed));
    }

    @Resource
    private SystemService systemService;

    @Resource
    private WebConfigurations webConf;

    @Resource
    private LoginInterceptor loginInterceptor;
}
