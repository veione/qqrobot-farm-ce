package de.honoka.qqrobot.farm.controller;

import de.honoka.qqrobot.farm.service.RegisterService;
import de.honoka.qqrobot.farm.system.ExtendRobotAttributes;
import de.honoka.qqrobot.spring.boot.starter.annotation.Command;
import de.honoka.qqrobot.spring.boot.starter.annotation.RobotController;
import de.honoka.qqrobot.spring.boot.starter.command.CommandMethodArgs;
import de.honoka.qqrobot.spring.boot.starter.component.RobotAttributes;
import de.honoka.qqrobot.spring.boot.starter.component.RobotBeanHolder;
import de.honoka.qqrobot.spring.boot.starter.component.util.RobotImageUtils;
import de.honoka.qqrobot.spring.boot.starter.property.RobotBasicProperties;

import javax.annotation.Resource;
import java.util.Objects;

@SuppressWarnings("unused")
@RobotController
public class BasicController {

    @Command(value = "登录密码", admin = true)
    public String getLoginPassword(CommandMethodArgs args) {
        robotBeanHolder.getFramework().sendGroupMsg(basicProperties
                .getDevelopingGroup(), ExtendRobotAttributes.WEB_LOGIN_PASSWORD);
        if(!Objects.equals(args.getGroup(), basicProperties.getDevelopingGroup()))
            return "请到开发群查看登录密码";
        return null;
    }

    @Command("帮助")
    public String help(CommandMethodArgs args) {
        return "请参阅\nhttps://github.com/kosaka-bun/honoka-docs/blob/master/qqrobot/qqrobot-farm/main.md";
    }

    @Command("注册")
    public String register(CommandMethodArgs args) {
        return registerService.requestRegister(args.getGroup(), args.getQq());
    }

    @Resource
    private RobotBeanHolder robotBeanHolder;

    @Resource
    private RobotBasicProperties basicProperties;

    @Resource
    private RobotImageUtils imageUtils;

    @Resource
    private RegisterService registerService;

    @Resource
    private RobotAttributes attributes;
}
