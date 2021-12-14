package de.honoka.qqrobot.farm.web.controller;

import de.honoka.qqrobot.farm.service.SystemService;
import de.honoka.qqrobot.framework.Framework;
import de.honoka.util.code.ActionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;

@Controller
@RequestMapping("/action")
public class ActionController {

    private final String AFTER_ACTION = "redirect:/console";

    @RequestMapping("/sendTestMessage")
    public String sendTestMessage() {
        ActionUtils.doAction("发送测试消息", systemService::sendTestMessage);
        return AFTER_ACTION;
    }

    @RequestMapping("/relogin")
    public String relogin() {
        ActionUtils.doAction("重新登录", framework::reboot);
        return AFTER_ACTION;
    }

    @Resource
    private SystemService systemService;

    @Resource
    private Framework framework;
}
