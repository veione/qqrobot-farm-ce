package de.honoka.qqrobot.farm.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import de.honoka.qqrobot.farm.common.StaticMessages;
import de.honoka.qqrobot.farm.common.UserStatus;
import de.honoka.qqrobot.farm.database.dao.LocationDao;
import de.honoka.qqrobot.farm.database.dao.TrainTicketDao;
import de.honoka.qqrobot.farm.database.dao.UserDao;
import de.honoka.qqrobot.farm.database.dao.UserTaskDao;
import de.honoka.qqrobot.farm.entity.farm.Location;
import de.honoka.qqrobot.farm.entity.farm.TrainTicket;
import de.honoka.qqrobot.farm.entity.farm.User;
import de.honoka.qqrobot.farm.entity.farm.UserTask;
import de.honoka.qqrobot.farm.util.IdGenerator;
import de.honoka.qqrobot.farm.util.JsonMaker;
import de.honoka.qqrobot.farm.util.LocationUtils;
import de.honoka.qqrobot.farm.util.train.ticket.TrainTicketUtils;
import de.honoka.qqrobot.spring.boot.starter.component.ExceptionReporter;
import de.honoka.qqrobot.spring.boot.starter.component.RobotBeanHolder;
import de.honoka.qqrobot.spring.boot.starter.component.session.RobotSession;
import de.honoka.qqrobot.spring.boot.starter.component.session.SessionManager;
import de.honoka.qqrobot.spring.boot.starter.component.util.RobotImageUtils;
import de.honoka.util.text.TextUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.*;

@Service
public class TrainTicketService {

    public static final int IMAGE_SIZE = 251;

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void removeOutOfDateTickets() {
        try {
            trainTicketDao.removeOutOfDateTickets();
        } catch(Exception e) {
            reporter.sendExceptionToDevelopingGroup(e);
        }
    }

    public void scheduleTrainArrive(long qq, int ticketId, Date arriveTime) {
        timer.schedule(new TimerTask() {
            //到达时执行
            @Override
            public void run() {
                trainTicketService.onTrainArrive(qq, ticketId);
            }
        }, arriveTime);
    }

    @Transactional
    public void onTrainArrive(long qq, int ticketId) {
        try {
            User u = userDao.findAndLock(qq);
            if(u == null) return;
            //判断用户动态是否正确
            if(!userService.getStatus(u).equals(UserStatus.ON_TRAIN)) return;
            //移除用户正在进行的任务
            userTaskDao.deleteById(u.getTaskId());
            u.setTaskId(null);
            userDao.setTaskIdNull(u);
            //修改用户所在的位置
            TrainTicket ticket = trainTicketDao.findAndLock(ticketId, qq);
            u.setLocationId(ticket.getToLocation());
            userDao.updateById(u);
            robotBeanHolder.getFramework().sendPrivateMsg(qq,
                    "你已到达" + ticket.getToStation() + "站");
        } catch(Exception e) {
            reporter.sendExceptionToDevelopingGroup(e);
        }
    }

    @Transactional
    public String boardTrain(long qq, int ticketId) {
        User u = userDao.findAndLock(qq);
        if(u == null) return StaticMessages.notRegistered;
        TrainTicket ticket = trainTicketDao.findAndLock(ticketId, qq);
        if(ticket == null) return "没有此车票";
        long now = System.currentTimeMillis();
        if(ticket.getUsed())
            return "该车票已检票";
        if(now < ticket.getStartTime().getTime() - 15 * 60 * 1000)
            return "该车次尚未开始检票";
        if(now > ticket.getStartTime().getTime() - 5 * 60 * 1000)
            return "该车次已停止检票";
        if(!u.getLocationId().equals(ticket.getFromLocation()))
            return "你当前不在该车票的始发地点";
        //检票
        ticket.setUsed(true);
        trainTicketDao.update(ticket);
        //添加任务
        UserTask task = new UserTask();
        task.setUserQq(qq);
        task.setType(UserStatus.ON_TRAIN);
        task.setStartTime(new Date(now));
        task.setFinishTime(ticket.getArriveTime());
        task.setReferencedParamsJson(JsonMaker.arbitrary(
                "ticket_id", ticket.getTicketId(),
                "arrive_time", TextUtils.getSimpleDateFormat()
                        .format(ticket.getArriveTime())
        ));
        userTaskDao.insert(task);
        u.setTaskId(userTaskDao.getLastInsertId());
        userDao.updateById(u);
        scheduleTrainArrive(qq, ticketId, ticket.getArriveTime());
        return "你已经上车，到达时间：" + TrainTicketUtils
                .getDisplayTimeStr(ticket.getArriveTime());
    }

    @Transactional
    public String boardTrainAheadOfTime(Long group, long qq, int ticketId) {
        //检查时间
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if(!(hour >= 23 || hour < 6))
            return "当前时间不能使用提前上车";
        User u = userDao.selectById(qq);
        if(u == null) return StaticMessages.notRegistered;
        TrainTicket ticket = trainTicketDao.selectOne(new QueryWrapper<>(
                new TrainTicket().setTicketId(ticketId).setUserQq(qq)));
        if(ticket == null) return "没有此车票";
        if(ticket.getUsed())
            return "该车票已检票";
        if(System.currentTimeMillis() > ticket.getStartTime()
                .getTime() - 5 * 60 * 1000)
            return "该车次已停止检票";
        if(!u.getLocationId().equals(ticket.getFromLocation()))
            return "你当前不在该车票的始发地点";
        //询问是否上车
        try(RobotSession session = sessionManager.openSession(group, qq)) {
            robotBeanHolder.getFramework().reply(group, qq, "是否花费" +
                    ticket.getPrice() + "资金提前上车？确认请发送“是”，回复任意其他" +
                    "内容，或30秒内不回复则取消本次会话");
            String reply = session.waitingForReply(30);
            if(!reply.equals("是")) return null;
            //执行
            u = userDao.findAndLock(qq);
            //扣费
            if(u.getAssets() < ticket.getPrice())
                return "你的资金不足，不能提前上车";
            u.reduceAssets(ticket.getPrice());
            userDao.updateById(u);
            //修改车票信息
            ticket = trainTicketDao.findAndLock(ticketId, qq);
            long now = System.currentTimeMillis();
            long trainLast = ticket.getArriveTime().getTime() -
                    ticket.getStartTime().getTime();
            ticket.setStartTime(new Date(now + 15 * 60 * 1000));
            ticket.setArriveTime(new Date(ticket.getStartTime().getTime() +
                    trainLast));
            trainTicketDao.update(ticket);
            //执行普通上车
            return boardTrain(qq, ticketId);
        } catch(RobotSession.TimeoutException te) {
            return null;
        }
    }

    public String getTickets(long qq) {
        User u = userDao.selectById(qq);
        if(u == null) return StaticMessages.notRegistered;
        List<TrainTicket> tickets = trainTicketDao.selectList(new QueryWrapper<>(
                new TrainTicket().setUserQq(qq)));
        if(tickets.size() <= 0) return "你还没有车票";
        String msg = "你拥有以下车票：\n";
        StringBuilder ticketsStr = new StringBuilder();
        for(int i = 0; i < tickets.size(); i++) {
            TrainTicket ticket = tickets.get(i);
            ticketsStr.append(String.format("%d.%s %s → %s\n%s ~ %s",
                    ticket.getTicketId(), ticket.getTrainNo(),
                    ticket.getFromStation(), ticket.getToStation(),
                    TrainTicketUtils.getDisplayTimeStr(ticket.getStartTime()),
                    TrainTicketUtils.getDisplayTimeStr(ticket.getArriveTime())
            ));
            if(i != tickets.size() - 1)
                ticketsStr.append("\n");
        }
        return msg + imageUtils.textToImageBySize(ticketsStr.toString(),
                IMAGE_SIZE);
    }

    @Transactional
    public String buyTicket(Long group, long qq, String province, String city) {
        User u = userDao.selectById(qq);
        if(u == null) return StaticMessages.notRegistered;
        //查询目的地
        Location toLocation = locationDao.findByProvinceAndCity(province, city);
        boolean isNewLocation = false;
        if(toLocation == null) {
            toLocation = LocationUtils.newLocation(province, city);
            if(toLocation == null) return "该地区无法无法获取到天气或地形，无法前往";
            isNewLocation = true;
        }
        //查询车票
        List<TrainTicket> tickets;
        try {
            Location fromLocation = locationDao.selectById(u.getLocationId());
            tickets = trainTicketUtils.getTickets(fromLocation.getCity(),
                    city, 5);
        } catch(Exception e) {
            reporter.sendExceptionToDevelopingGroup(e);
            return "数据解析失败，可能是网络不佳，请稍后再试";
        }
        if(tickets.size() <= 0)
            return "没有查询到从你的位置到指定地区的车票，请考虑多线路换乘";
        //询问购买
        try(RobotSession session = sessionManager.openSession(group, qq)) {
            String msg = getTicketsMessage(tickets) + "\n";
            msg += "请回复要购买的序号，回复任意其他内容，或30秒内不回复则取消本次会话";
            robotBeanHolder.getFramework().reply(group, qq, msg);
            String reply = session.waitingForReply(30).trim();
            int index = Integer.parseInt(reply) - 1;
            //判断序号正确性
            if(index < 0 || index >= tickets.size()) return null;
            TrainTicket ticket = tickets.get(index);
            //判断余额
            if(u.getAssets() < ticket.getPrice()) return "你的资金不足";
            //生成ID
            Integer ticketId = IdGenerator.generateInteger(2000, id -> {
                trainTicketDao.insert(new TrainTicket().setTicketId(id)
                        .setUserQq(qq));
            });
            if(ticketId == null) return "现有车票较多，暂不能购买车票";
            //购买
            ticket.setTicketId(ticketId);
            ticket.setUserQq(qq);
            ticket.setFromLocation(u.getLocationId());
            ticket.setToLocation(toLocation.getLocationId());
            trainTicketDao.update(ticket);
            //扣费
            u.reduceAssets(ticket.getPrice());
            userDao.updateById(u);
            //判断是否保存新位置
            if(isNewLocation) locationDao.insert(toLocation);
            return "你成功购买了" + ticket.getTrainNo() + "次列车的车票，资金余额为：" +
                    u.getAssets() + "\n请在开车前15分钟到开车前5分钟的时间段内执行上车操作";
        } catch(RobotSession.TimeoutException | NumberFormatException e) {
            //不执行操作
            return null;
        }
    }

    public String queryAvaliableTickets(long qq, String province, String city) {
        User u = userDao.selectById(qq);
        if(u == null) return StaticMessages.notRegistered;
        //查询目的地
        Location toLocation = locationDao.findByProvinceAndCity(province, city);
        if(toLocation == null) {
            toLocation = LocationUtils.newLocation(province, city);
            if(toLocation == null) return "该地区无法无法获取到天气或地形，无法前往";
        }
        //查询车票
        List<TrainTicket> tickets;
        try {
            Location userLocation = locationDao.selectById(u.getLocationId());
            tickets = trainTicketUtils.getTickets(userLocation.getCity(),
                    city, 5);
        } catch(Exception e) {
            reporter.sendExceptionToDevelopingGroup(e);
            return "数据解析失败，可能是网络不佳，请稍后再试";
        }
        if(tickets.size() <= 0)
            return "没有查询到从你的位置到指定地区的车票，请考虑多线路换乘";
        //返回车票
        return getTicketsMessage(tickets);
    }

    private String getTicketsMessage(List<TrainTicket> tickets) {
        String msg = "找到以下可用车票：\n";
        StringBuilder ticketsStr = new StringBuilder();
        for(int i = 0; i < tickets.size(); i++) {
            TrainTicket ticket = tickets.get(i);
            ticketsStr.append(String.format(
                    "%d.%s %s → %s\n%s ~ %s\n票价：%d",
                    i + 1, ticket.getTrainNo(), ticket.getFromStation(),
                    ticket.getToStation(),
                    TrainTicketUtils.getDisplayTimeStr(ticket.getStartTime()),
                    TrainTicketUtils.getDisplayTimeStr(ticket.getArriveTime()),
                    ticket.getPrice()
            ));
            if(i != tickets.size() - 1)
                ticketsStr.append("\n");
        }
        return msg + imageUtils.textToImageBySize(ticketsStr.toString(),
                IMAGE_SIZE);
    }

    @Resource
    private LocationDao locationDao;

    @Resource
    private UserTaskDao userTaskDao;

    @Resource
    private UserService userService;

    @Resource
    private UserDao userDao;

    @Resource
    private TrainTicketService trainTicketService;

    @Resource
    private RobotBeanHolder robotBeanHolder;

    @Resource
    private RobotImageUtils imageUtils;

    @Resource
    private ExceptionReporter reporter;

    @Resource
    private Timer timer;

    @Resource
    private TrainTicketUtils trainTicketUtils;

    @Resource
    private SessionManager sessionManager;

    @Resource
    private TrainTicketDao trainTicketDao;
}
