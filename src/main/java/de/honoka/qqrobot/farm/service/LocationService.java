package de.honoka.qqrobot.farm.service;

import de.honoka.qqrobot.farm.common.StaticMessages;
import de.honoka.qqrobot.farm.database.dao.LocationDao;
import de.honoka.qqrobot.farm.database.dao.UserDao;
import de.honoka.qqrobot.farm.entity.farm.Location;
import de.honoka.qqrobot.farm.entity.farm.User;
import de.honoka.qqrobot.framework.Framework;
import de.honoka.qqrobot.spring.boot.starter.component.ExceptionReporter;
import de.honoka.qqrobot.spring.boot.starter.component.session.RobotSession;
import de.honoka.qqrobot.spring.boot.starter.component.session.SessionManager;
import de.honoka.util.various.ListRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.List;

@Service
public class LocationService {

    private static final int
            CONTRACT_COST = 70_0000,
            SHOP_MAX_SIZE = 20,
            LAND_MAX_SIZE = 20;

    @Transactional
    public String extendShop(Long group, long qq) {
        User u = userDao.selectById(qq);
        if(u == null) return StaticMessages.notRegistered;
        Location location = locationDao.selectById(u.getLocationId());
        if(!u.getQq().equals(location.getLandlordQq()))
            return "你不是你所在地的地主，不能扩建商店";
        int price = location.getExtendShopPrice();
        if(u.getAssets() < price)
            return "你的资金不足" + price + "，无法扩建";
        if(location.getShopVolume() >= SHOP_MAX_SIZE)
            return "当前地区已达到最大商店容量";
        try(RobotSession session = sessionManager.openSession(group, qq)) {
            framework.reply(group, qq, "是否花费" + price +
                    "资金扩建" + location.getFriendlyLocation() +
                    "的商店？确认请发送“是”，发送任意其他内容取消");
            String reply = session.waitingForReply(60);
            if(!reply.equals("是"))
                throw new RobotSession.TimeoutException();
            //扩建商店
            u = userDao.findAndLock(qq);
            location = locationDao.findAndLock(u.getLocationId());
            u.reduceAssets(price);
            location.plusShopVolume();
            userDao.updateById(u);
            locationDao.updateById(location);
            return "扩建成功，" + location.getFriendlyLocation() +
                    "现在的商店大小为" + location.getShopVolume();
        } catch(RobotSession.TimeoutException te) {
            return "已取消扩建";
        }
    }

    @Transactional
    public String extendLand(Long group, long qq) {
        User u = userDao.selectById(qq);
        if(u == null) return StaticMessages.notRegistered;
        Location location = locationDao.selectById(u.getLocationId());
        if(!u.getQq().equals(location.getLandlordQq()))
            return "你不是你所在地的地主，不能扩建土地";
        int price = location.getExtendLandPrice();
        if(u.getAssets() < price)
            return "你的资金不足" + price + "，无法扩建";
        if(location.getLandSize() >= LAND_MAX_SIZE)
            return "当前地区已达到最大土地数量";
        try(RobotSession session = sessionManager.openSession(group, qq)) {
            framework.reply(group, qq, "是否花费" + price +
                    "资金扩建" + location.getFriendlyLocation() +
                    "的土地？确认请发送“是”，发送任意其他内容取消");
            String reply = session.waitingForReply(60);
            if(!reply.equals("是"))
                throw new RobotSession.TimeoutException();
            //扩建
            u = userDao.findAndLock(qq);
            location = locationDao.findAndLock(u.getLocationId());
            u.reduceAssets(price);
            location.plusLandSize();
            return "扩建成功，" + location.getFriendlyLocation() +
                    "现在的土地大小为" + location.getLandSize();
        } catch(RobotSession.TimeoutException te) {
            return "已取消扩建";
        }
    }

    @Transactional
    public String setRental(long qq, int rental) {
        User u = userDao.selectById(qq);
        if(u == null) return StaticMessages.notRegistered;
        Location location = locationDao.findAndLock(u.getLocationId());
        if(!u.getQq().equals(location.getLandlordQq())) {
            return "你不是" + location.getFriendlyLocation() +
                    "的地主，不能设置租金";
        }
        //设置租金
        if(rental <= 0) rental = 1;
        location.setRental(rental);
        locationDao.updateById(location);
        return "设置成功，" + location.getFriendlyLocation() +
                "的租金现在为" + location.getRental();
    }

    @Transactional
    public String contract(Long group, long qq) {
        User u = userDao.selectById(qq);
        if(u == null) return StaticMessages.notRegistered;
        Location location = locationDao.selectById(u.getLocationId());
        if(u.getAssets() < CONTRACT_COST)
            return "你的资金不足" + CONTRACT_COST + "，无法承包当前地区";
        if(location.getLandlordQq() != null) {
            return "你的所在地已被" + framework.getNickOrCard(group, qq) +
                    "承包";
        }
        try(RobotSession session = sessionManager.openSession(group, qq)) {
            framework.reply(group, qq, "是否花费" + CONTRACT_COST +
                    "资金承包" + location.getFriendlyLocation() +
                    "？确认请发送“是”，发送任意其他内容取消");
            String reply = session.waitingForReply(60);
            if(!reply.equals("是"))
                throw new RobotSession.TimeoutException();
            //承包
            u = userDao.findAndLock(qq);
            location = locationDao.findAndLock(u.getLocationId());
            u.reduceAssets(CONTRACT_COST);
            location.setLandlordQq(qq);
            userDao.updateById(u);
            locationDao.updateById(location);
            return "你已成为" + location.getFriendlyLocation() + "的地主";
        } catch(RobotSession.TimeoutException te) {
            return "已取消承包";
        }
    }

    @Scheduled(cron = "0 5/10 * * * ?")
    public synchronized void updateAllWeather() {
        List<Integer> idList = locationDao.findAllId();
        for(Integer id : idList) {
            try {
                locationService.updateWeather(id);
            } catch(Exception e) {
                reporter.sendExceptionToDevelopingGroup(e);
            }
        }
    }

    @Transactional
    public void updateWeather(int id) {
        //更新前先加锁
        Location location = locationDao.findAndLock(id);
        location.updateWeather();
        locationDao.updateById(location);
    }

    public LocationService(ListRunner initer) {
        initer.add(this::updateAllWeather);
    }

    @Resource
    private LocationService locationService;

    @Resource
    private LocationDao locationDao;

    @Resource
    private SessionManager sessionManager;

    @Resource
    private Framework framework;

    @Resource
    private ExceptionReporter reporter;

    @Resource
    private UserDao userDao;
}
