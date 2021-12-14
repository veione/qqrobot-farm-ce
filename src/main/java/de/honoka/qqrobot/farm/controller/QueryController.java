package de.honoka.qqrobot.farm.controller;

import de.honoka.qqrobot.farm.service.*;
import de.honoka.qqrobot.spring.boot.starter.annotation.Command;
import de.honoka.qqrobot.spring.boot.starter.annotation.RobotController;
import de.honoka.qqrobot.spring.boot.starter.command.CommandMethodArgs;

import javax.annotation.Resource;

@SuppressWarnings("unused")
@RobotController
public class QueryController {

    @Command(value = "所有补偿申请", admin = true)
    public String getAllCompensationRequest(CommandMethodArgs args) {
        return compensationService.queryRequsets();
    }

    @Command("我的补偿申请")
    public String myCompensationRequest(CommandMethodArgs args) {
        return compensationService.queryRequests(args.getQq());
    }

    @Command(value = "查询车票", argsNum = 1)
    public String queryTickets(CommandMethodArgs args) {
        if(args.getArgs().length == 1)
            return trainTicketService.queryAvaliableTickets(args.getQq(),
                    args.getString(0), args.getString(0));
        return trainTicketService.queryAvaliableTickets(args.getQq(),
                args.getString(0), args.getString(1));
    }

    @Command("我的车票")
    public String myTickets(CommandMethodArgs args) {
        return trainTicketService.getTickets(args.getQq());
    }

    @Command("果实背包")
    public String fruitBag(CommandMethodArgs args) {
        return fruitBagService.getBag(args.getQq());
    }

    @Command("作物")
    public String crop(CommandMethodArgs args) {
        return cropService.getCrops(args.getGroup(), args.getQq());
    }

    @Command("种子背包")
    public String seedBag(CommandMethodArgs args) {
        return seedBagService.getBag(args.getQq());
    }

    @Command("种子商店")
    public String seedShop(CommandMethodArgs args) {
        return seedShopService.getShop(args.getQq());
    }

    @Command("我的信息")
    public String myInfo(CommandMethodArgs args) {
        return userService.getSelfInfo(args.getQq());
    }

    @Resource
    private CompensationService compensationService;

    @Resource
    private TrainTicketService trainTicketService;

    @Resource
    private FruitBagService fruitBagService;

    @Resource
    private CropService cropService;

    @Resource
    private SeedBagService seedBagService;

    @Resource
    private SeedShopService seedShopService;

    @Resource
    private UserService userService;
}
