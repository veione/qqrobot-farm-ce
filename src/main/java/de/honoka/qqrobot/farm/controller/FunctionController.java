package de.honoka.qqrobot.farm.controller;

import de.honoka.qqrobot.farm.service.CompensationService;
import de.honoka.qqrobot.farm.service.SeedShopService;
import de.honoka.qqrobot.farm.service.UserService;
import de.honoka.qqrobot.spring.boot.starter.annotation.Command;
import de.honoka.qqrobot.spring.boot.starter.annotation.RobotController;
import de.honoka.qqrobot.spring.boot.starter.command.CommandMethodArgs;
import de.honoka.qqrobot.spring.boot.starter.component.RobotAttributes;

import javax.annotation.Resource;

import static com.sobte.cqp.jcq.event.JcqApp.CC;

@SuppressWarnings("unused")
@RobotController
public class FunctionController {

    @Command(value = "转账", argsNum = 2)
    public String assetsTransfer(CommandMethodArgs args) {
        int assets = args.getInt(0);
        long atQQ = CC.getAt(args.getString(1));
        if(atQQ == -1000L) return "你还没有at目标";
        return userService.assetsTransfer(args.getQq(), atQQ, assets);
    }

    @Command(value = "传送", argsNum = 2, admin = true)
    public String transmit(CommandMethodArgs args) {
        int num = args.getArgs().length;
        if(num >= 3) {
            long atQQ = CC.getAt(args.getString(2));
            if(atQQ == -1000L) return "你还没有at目标";
            return userService.transmit(atQQ,
                    args.getString(0), args.getString(1));
        } else {
            long atQQ = CC.getAt(args.getString(1));
            if(atQQ == -1000L) return "你还没有at目标";
            return userService.transmit(atQQ,
                    args.getString(0), args.getString(0));
        }
    }

    @Command(value = "处理补偿申请", admin = true, argsNum = 2)
    public String executeCompensationRequest(CommandMethodArgs args) {
        String opt = args.getString(0);
        int requestId = args.getInt(1);
        switch(opt) {
            case "同意":
                return compensationService.agreeRequest(requestId);
            case "移除":
                return compensationService.removeRequest(requestId);
            default:
                return attributes.wrongParameterMsg;
        }
    }

    @Command(value = "申请补偿", argsNum = 1)
    public String requestCompensation(CommandMethodArgs args) {
        return compensationService.request(args.getQq(), args.getInt(0));
    }

    @Command(value = "增加资金", argsNum = 2, admin = true)
    public String makeAssets(CommandMethodArgs args) {
        long atQQ = CC.getAt(args.getString(1));
        if(atQQ == -1000L) return "你还没有at目标";
        return userService.makeAssets(atQQ, args.getInt(0));
    }

    @Command(value = "更新商店", admin = true)
    public String updateAllShop(CommandMethodArgs args) {
        seedShopService.updateAllShop();
        return "更新完成";
    }

    @Resource
    private UserService userService;

    @Resource
    private SeedShopService seedShopService;

    @Resource
    private RobotAttributes attributes;

    @Resource
    private CompensationService compensationService;
}
