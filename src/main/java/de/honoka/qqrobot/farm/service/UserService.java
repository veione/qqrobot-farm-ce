package de.honoka.qqrobot.farm.service;

import com.google.gson.JsonObject;
import de.honoka.qqrobot.farm.common.StaticMessages;
import de.honoka.qqrobot.farm.common.UserStatus;
import de.honoka.qqrobot.farm.database.dao.*;
import de.honoka.qqrobot.farm.entity.farm.ChangeLocationRequest;
import de.honoka.qqrobot.farm.entity.farm.Location;
import de.honoka.qqrobot.farm.entity.farm.User;
import de.honoka.qqrobot.farm.entity.farm.UserTask;
import de.honoka.qqrobot.farm.util.LocationUtils;
import de.honoka.qqrobot.farm.web.WebConfigurations;
import de.honoka.qqrobot.spring.boot.starter.component.ExceptionReporter;
import de.honoka.qqrobot.spring.boot.starter.component.RobotBeanHolder;
import de.honoka.qqrobot.spring.boot.starter.component.session.RobotSession;
import de.honoka.qqrobot.spring.boot.starter.component.session.SessionManager;
import de.honoka.util.text.TextUtils;
import de.honoka.util.various.ListRunner;
import lombok.SneakyThrows;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@Service
public class UserService {

    public static final int
            SUBSIDY_VALUE = 3000,    //一次发放的补贴数量
            MAX_LEVEL = 20;

    /**
     * 转账
     */
    @Transactional
    public String assetsTransfer(long fromQQ, long toQQ, int assets) {
        User from = userDao.findAndLock(fromQQ);
        if(from == null) return StaticMessages.notRegistered;
        User to = userDao.findAndLock(toQQ);
        if(to == null) return "目标用户尚未注册";
        //指定金额大于余额
        if(assets > from.getAssets()) assets = from.getAssets();
        if(assets <= 0) return "指定的金额有误";
        //执行转账
        from.reduceAssets(assets);
        to.plusAssets(assets);
        userDao.updateById(from);
        userDao.updateById(to);
        return "转账成功，你的资金余额为：" + from.getAssets();
    }

    /**
     * 定时清除不合法任务
     */
    @Scheduled(cron = "0 0/30 * * * ?")
    public void removeInvalidTasks() {
        List<User> users = userDao.selectList(null);
        for(User u : users) {
            try {
                userService.removeInvalidTask(u);
            } catch(Exception e) {
                reporter.sendExceptionToDevelopingGroup(e);
            }
        }
    }

    @Transactional
    public void removeInvalidTask(User u) {
        u = userDao.findAndLock(u.getQq());
        if(u.getTaskId() == null) return;
        UserTask task = userTaskDao.selectById(u.getTaskId());
        if(task == null) {
            userDao.setTaskIdNull(u);
            return;
        }
        //如果已经超过约定完成时间十分钟以上
        if(System.currentTimeMillis() - task.getFinishTime().getTime()
                > 10 * 60 * 1000) {
            userDao.setTaskIdNull(u);
            userTaskDao.deleteById(task);
        }
    }

    /**
     * 每天0点为低资金用户发放补贴
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void giveSubsidy() {
        List<User> users = userDao.selectList(null);
        for(User u : users) {
            try {
                userService.giveSubsidy(u);
            } catch(Exception e) {
                reporter.sendExceptionToDevelopingGroup(e);
            }
        }
    }

    @Transactional
    public void giveSubsidy(User u) {
        u = userDao.findAndLock(u.getQq());
        if(u.getAssets() >= 1000) return;
        if(seedOfUserDao.getCountOfUser(u.getQq()) > 0) return;
        if(fruitDao.getCountOfUser(u.getQq()) > 0) return;
        u.plusAssets(SUBSIDY_VALUE);
        userDao.updateById(u);
    }

    @Transactional
    public String updateLevel(Long group, long qq) {
        User u = userDao.selectById(qq);
        if(u == null) return StaticMessages.notRegistered;
        if(u.getLevel() >= MAX_LEVEL) return "你已经升到最高等级";
        int price = u.getUpdateLevelPrice();
        if(u.getAssets() < price) return "你的资金不足" + price + "，无法升级";
        try(RobotSession session = sessionManager.openSession(group, qq)) {
            robotBeanHolder.getFramework().reply(group, qq, String.format(
                    "是否花费资金%d来升到%d级？确认请发送“是”，发送任意其他内容取消",
                    price, u.getLevel() + 1));
            String msg = session.waitingForReply(60);
            if(!msg.equals("是"))
                throw new RobotSession.TimeoutException();
            u = userDao.findAndLock(qq);
            u.reduceAssets(price);
            u.setLevel(u.getLevel() + 1);
            userDao.updateById(u);
            return "升级成功\n你已升到" + u.getLevel() + "级\n资金余额为：" +
                    u.getAssets();
        } catch(RobotSession.TimeoutException e) {
            return "已取消升级";
        }
    }

    /**
     * 为指定用户增加资金
     */
    @Transactional
    public String makeAssets(long qq, int assetsInc) {
        if(assetsInc <= 0) return "要增加的资金量不是正数";
        User u = userDao.findAndLock(qq);
        if(u == null) return "指定的用户尚未注册";
        u.plusAssets(assetsInc);
        userDao.updateById(u);
        return "增加成功\n该用户现在的资金剩余为：" + u.getAssets();
    }

    public String getSelfInfo(long qq) {
        User u = userDao.selectById(qq);
        if(u == null) return StaticMessages.notRegistered;
        String reply = "你当前的用户信息如下：";
        reply += "\n等级：Lv" + u.getLevel();
        reply += "\n资产：" + u.getAssets();
        reply += "\n水桶余水量：" + u.getWaterRemaining() + "/" +
                u.getMaxWaterRemaining();
        Location location = locationDao.selectById(u.getLocationId());
        reply += "\n所在地：" + location.getFriendlyLocation();
        reply += "\n所在地天气：" + location.getNowWeather();
        reply += "\n所在地地形：" + location.getLandform();
        reply += "\n状态：" + getStatus(u);
        return reply;
    }

    public String getStatus(User u) {
        if(u.getTaskId() == null) return UserStatus.FREE;
        return userTaskDao.selectById(u.getTaskId()).getType();
    }

    @Transactional
    public String transmit(long qq, String province, String city) {
        User u = userDao.findAndLock(qq);
        if(u == null) return StaticMessages.notRegistered;
        //获取数据库中已有的关于此地理位置的信息
        Location location = locationDao.findByProvinceAndCity(province, city);
        //若没有，则新建此地理位置的信息
        if(location == null) {
            location = LocationUtils.newLocation(province, city);
            //无法获取到此位置的天气或地形
            if(location == null)
                return "指定的区域无法获取到天气或地形";
            locationDao.insert(location);
            location = locationDao.findByProvinceAndCity(province, city);
        }
        //更新用户的地理位置
        u.setLocationId(location.getLocationId());
        userDao.updateById(u);
        return "传送成功";
    }

    /**
     * 执行地理位置更新
     */
    @Transactional
    public String changeLocation(String id, String ip) {
        //检验注册请求ID是否合法
        ChangeLocationRequest clr = changeLocationRequestDao.findAndLock(id);
        if(clr == null) return "没有找到更新请求";
        //已过期
        if(System.currentTimeMillis() > clr.getOverdueTime().getTime())
            return "此更新请求已过期，请重新发起更新请求";
        //ID合法
        //确认是否已有用户信息
        User u = userDao.findAndLock(clr.getRequestQq());
        if(u == null) return StaticMessages.notRegistered;
        //获取IP的地理位置
        String[] location0 = LocationUtils.ipLocationQuery(ip);
        if(location0 == null) return "没有找到你的IP地址的所在地，暂不能更新位置";
        //获取数据库中已有的关于此地理位置的信息
        Location location = locationDao.findByProvinceAndCity(
                location0[0], location0[1]);
        //若没有，则新建此地理位置的信息
        if(location == null) {
            location = LocationUtils.newLocation(location0[0], location0[1]);
            //无法获取到此位置的天气或地形
            if(location == null)
                return "你的IP地址所在区域无法获取到天气或地形";
            locationDao.insert(location);
            location = locationDao.findByProvinceAndCity(
                    location0[0], location0[1]);
        }
        //更新用户的地理位置
        u.setLocationId(location.getLocationId());
        userDao.updateById(u);
        //调整请求ID的成功状态
        clr.setSuccessTime(new Date());
        changeLocationRequestDao.updateById(clr);
        return "更新成功\n你现在的地理位置为：" + location.getFriendlyLocation() +
                "\n地形为：" + location.getLandform();
    }

    /**
     * 请求更新地理位置
     */
    @Transactional
    public String requestChangeLocation(Long group, long qq) {
        User u = userDao.selectById(qq);
        if(u == null) return StaticMessages.notRegistered;
        //查找是否有现有可用的更新请求
        ChangeLocationRequest clr = changeLocationRequestDao
                .getAvaliableRequest(qq);
        //若没有，则新建请求，并存储
        if(clr == null) {
            //判断上一次成功时间与当前时间的距离
            ChangeLocationRequest last = changeLocationRequestDao
                    .getLatestSuccessRequest(qq);
            if(last != null && System.currentTimeMillis() - last.getSuccessTime()
                    .getTime() < 10 * 24 * 60 * 60 * 1000) {
                return "距上一次更新位置不足10天，暂不能更新位置";
            }
            //符合要求，新建请求
            clr = new ChangeLocationRequest();
            clr.setRequestQq(qq);
            clr.setOverdueTime(new Date(System.currentTimeMillis() + 300 * 1000));
            changeLocationRequestDao.insert(clr);
        }
        //发送已生成的更新链接
        //honoka.de/qqrobot-farm/changeLocation?id=xxx
        String url = "http://www.honoka.de" + webConf.getContextPath() +
                "/changeLocation?id=" + clr.getRequestId();
        robotBeanHolder.getFramework().sendPrivateMsg(qq,
                "请访问下面的链接完成地理位置更新\n" + url);
        if(group != null) return "更新链接已通过私聊发送";
        return null;
    }

    /**
     * 启动时加载所有未完成的用户任务
     */
    public void loadAllUserTasks() {
        List<User> users = userDao.selectList(null);
        for(User u : users) {
            try {
                userService.loadUserTask(u);
            } catch(Throwable t) {
                reporter.sendExceptionToDevelopingGroup(t);
            }
        }
    }

    @Transactional
    @SneakyThrows
    public void loadUserTask(User u) {
        u = userDao.findAndLock(u.getQq());
        if(u.getTaskId() == null) return;
        UserTask task = userTaskDao.findAndLock(u.getTaskId());
        if(task == null) {
            userDao.setTaskIdNull(u);
            return;
        }
        switch(task.getType()) {
            case UserStatus.FETCHING_WATER:
                scheduleFetchWater(u.getQq(), task.getFinishTime());
                break;
            case UserStatus.ON_TRAIN:
                JsonObject params = task.getReferencedParamsJson();
                int ticketId = params.get("ticket_id").getAsInt();
                Date arriveTime = TextUtils.getSimpleDateFormat()
                        .parse(params.get("arrive_time").getAsString());
                trainTicketService.scheduleTrainArrive(u.getQq(), ticketId,
                        arriveTime);
                break;
            default:
                //修正不正确的任务记录
                userDao.setTaskIdNull(u);
                userTaskDao.deleteById(task);
                break;
        }
    }

    private void scheduleFetchWater(long qq, Date time) {
        timer.schedule(new TimerTask() {

            //打水完成时执行
            @Override
            public void run() {
                try {
                    userService.onFetchWaterFinished(qq);
                } catch(Throwable t) {
                    reporter.sendExceptionToDevelopingGroup(t);
                }
            }
        }, time);
    }

    @Transactional
    public void onFetchWaterFinished(long qq) {
        //查找用户
        User u = userDao.findAndLock(qq);
        if(u == null || u.getTaskId() == null) return;
        //判断用户动态是否正确
        UserTask task = userTaskDao.findAndLock(u.getTaskId());
        if(!task.getType().equals(UserStatus.FETCHING_WATER)) return;
        //移除用户正在进行的任务
        u.setTaskId(null);
        userDao.setTaskIdNull(u);
        userTaskDao.deleteById(task);
        //补充水量
        u.setWaterRemaining(u.getMaxWaterRemaining());
        userDao.updateById(u);
    }

    /**
     * 打水
     */
    @Transactional
    public String fetchWater(long qq) {
        User u = userDao.findAndLock(qq);
        if(u == null) return StaticMessages.notRegistered;
        //判断水桶容量
        if(u.getWaterRemaining() >= u.getMaxWaterRemaining())
            return "你的水桶是满的，不需要打水";
        //添加任务项，然后执行计划打水
        UserTask task = new UserTask();
        task.setUserQq(qq);
        task.setType(UserStatus.FETCHING_WATER);
        long now = System.currentTimeMillis();
        task.setStartTime(new Date(now));
        task.setFinishTime(new Date(now + 30 * 60 * 1000));
        userTaskDao.insert(task);
        u.setTaskId(userTaskDao.getLastInsertId());
        userDao.updateById(u);
        scheduleFetchWater(qq, task.getFinishTime());
        return "已开始打水";
    }

    public UserService(ListRunner initer) {
        initer.add(this::loadAllUserTasks);
    }

    @Resource
    private ChangeLocationRequestDao changeLocationRequestDao;

    @Resource
    private LocationDao locationDao;

    @Resource
    private FruitDao fruitDao;

    @Resource
    private SeedOfUserDao seedOfUserDao;

    @Resource
    private UserTaskDao userTaskDao;

    @Resource
    private UserService userService;

    @Resource
    private RobotBeanHolder robotBeanHolder;

    @Resource
    private WebConfigurations webConf;

    @Resource
    private ExceptionReporter reporter;

    @Resource
    private TrainTicketService trainTicketService;

    @Resource
    private SessionManager sessionManager;

    @Resource
    private Timer timer;

    @Resource
    private UserDao userDao;
}
