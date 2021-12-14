package de.honoka.qqrobot.farm.controller;

import de.honoka.qqrobot.farm.service.*;
import de.honoka.qqrobot.farm.util.ParameterUtils;
import de.honoka.qqrobot.spring.boot.starter.annotation.Command;
import de.honoka.qqrobot.spring.boot.starter.annotation.RobotController;
import de.honoka.qqrobot.spring.boot.starter.command.CommandMethodArgs;
import de.honoka.qqrobot.spring.boot.starter.component.RobotAttributes;

import javax.annotation.Resource;

/**
 * 仅空闲时可以执行的操作，通过切面类来实现
 */
@SuppressWarnings("unused")
@RobotController
public class OnFreeController {

    @Command("扩建商店")
    public String extendShop(CommandMethodArgs args) {
        return locationService.extendShop(args.getGroup(), args.getQq());
    }

    @Command("扩建土地")
    public String extendLand(CommandMethodArgs args) {
        return locationService.extendLand(args.getGroup(), args.getQq());
    }

    @Command(value = "设置租金", argsNum = 1)
    public String setRental(CommandMethodArgs args) {
        return locationService.setRental(args.getQq(), args.getInt(0));
    }

    @Command("承包")
    public String contract(CommandMethodArgs args) {
        return locationService.contract(args.getGroup(), args.getQq());
    }

    @Command(value = "提前上车", argsNum = 1)
    public String boardTrainAheadOfTime(CommandMethodArgs args) {
        return trainTicketService.boardTrainAheadOfTime(
                args.getGroup(), args.getQq(), args.getInt(0));
    }

    @Command(value = "上车", argsNum = 1)
    public String boardTrain(CommandMethodArgs args) {
        return trainTicketService.boardTrain(args.getQq(), args.getInt(0));
    }

    @Command(value = "购买车票", argsNum = 1)
    public String buyTrainTicket(CommandMethodArgs args) {
        if(args.getArgs().length == 1)
            return trainTicketService.buyTicket(args.getGroup(), args.getQq(),
                    args.getString(0), args.getString(0));
        return trainTicketService.buyTicket(args.getGroup(), args.getQq(),
                args.getString(0), args.getString(1));
    }

    @Command("升级")
    public String updateLevel(CommandMethodArgs args) {
        return userService.updateLevel(args.getGroup(), args.getQq());
    }

    @Command("更新位置")
    public String changeLocation(CommandMethodArgs args) {
        return userService.requestChangeLocation(args.getGroup(), args.getQq());
    }

    @Command("打水")
    public String fetchWater(CommandMethodArgs args) {
        return userService.fetchWater(args.getQq());
    }

    @Command(value = "购买种子", argsNum = 1)
    public String buySeed(CommandMethodArgs args) {
        int argsNum = args.getArgs().length;
        if(args.getString(0).equals("范围")) {
            if(args.getArgs().length < 3)
                return RobotAttributes.parameterNotEnoughMsg;
            else
                return seedShopService.buySeed(args.getQq(), ParameterUtils
                        .getSequence(args.getInt(1), args.getInt(2)));
        } else {
            if(argsNum == 1)
                return seedShopService.buySeed(args.getQq(), args.getInt(0));
            else
                return seedShopService.buySeed(args.getQq(), ParameterUtils
                        .toIntArr(args.getArgs()));
        }
    }

    @Command(value = "卖出", argsNum = 1)
    public String sell(CommandMethodArgs args) {
        int argsNum = args.getArgs().length;
        if(args.getString(0).equals("范围")) {
            if(args.getArgs().length < 3)
                return RobotAttributes.parameterNotEnoughMsg;
            else
                return fruitBagService.sell(args.getQq(), ParameterUtils
                        .getSequence(args.getInt(1), args.getInt(2)));
        } else {
            if(argsNum == 1)
                return fruitBagService.sell(args.getQq(), args.getInt(0));
            else
                return fruitBagService.sell(args.getQq(), ParameterUtils
                        .toIntArr(args.getArgs()));
        }
    }

    @Command(value = "清除", argsNum = 1)
    public String remove(CommandMethodArgs args) {
        int argsNum = args.getArgs().length;
        if(args.getString(0).equals("范围")) {
            if(args.getArgs().length < 3)
                return RobotAttributes.parameterNotEnoughMsg;
            else
                return cropService.remove(args.getQq(), ParameterUtils
                        .getSequence(args.getInt(1), args.getInt(2)));
        } else {
            if(argsNum == 1)
                return cropService.remove(args.getQq(), args.getInt(0));
            else
                return cropService.remove(args.getQq(), ParameterUtils
                        .toIntArr(args.getArgs()));
        }
    }

    @Command(value = "收获", argsNum = 1)
    public String harvest(CommandMethodArgs args) {
        int argsNum = args.getArgs().length;
        if(args.getString(0).equals("范围")) {
            if(args.getArgs().length < 3)
                return RobotAttributes.parameterNotEnoughMsg;
            else
                return cropService.harvest(args.getQq(), ParameterUtils
                        .getSequence(args.getInt(1), args.getInt(2)));
        } else {
            if(argsNum == 1)
                return cropService.harvest(args.getQq(), args.getInt(0));
            else
                return cropService.harvest(args.getQq(), ParameterUtils
                        .toIntArr(args.getArgs()));
        }
    }

    @Command(value = "浇水", argsNum = 1)
    public String watering(CommandMethodArgs args) {
        int argsNum = args.getArgs().length;
        if(args.getString(0).equals("范围")) {
            if(args.getArgs().length < 3)
                return RobotAttributes.parameterNotEnoughMsg;
            else
                return cropService.watering(args.getQq(), ParameterUtils
                        .getSequence(args.getInt(1), args.getInt(2)));
        } else {
            if(argsNum == 1)
                return cropService.watering(args.getQq(), args.getInt(0));
            else
                return cropService.watering(args.getQq(), ParameterUtils
                        .toIntArr(args.getArgs()));
        }
    }

    @Command(value = "播种", argsNum = 1)
    public String sowSeed(CommandMethodArgs args) {
        int argsNum = args.getArgs().length;
        if(args.getString(0).equals("范围")) {
            if(args.getArgs().length < 3)
                return RobotAttributes.parameterNotEnoughMsg;
            else
                return cropService.sowSeed(args.getQq(), ParameterUtils
                        .getSequence(args.getInt(1), args.getInt(2)));
        } else {
            if(argsNum == 1)
                return cropService.sowSeed(args.getQq(), args.getInt(0));
            else
                return cropService.sowSeed(args.getQq(), ParameterUtils
                        .toIntArr(args.getArgs()));
        }
    }

    @Resource
    private LocationService locationService;

    @Resource
    private TrainTicketService trainTicketService;

    @Resource
    private RobotAttributes attributes;

    @Resource
    private UserService userService;

    @Resource
    private SeedShopService seedShopService;

    @Resource
    private FruitBagService fruitBagService;

    @Resource
    private CropService cropService;
}
