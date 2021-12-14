package de.honoka.qqrobot.farm.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import de.honoka.qqrobot.farm.common.StaticMessages;
import de.honoka.qqrobot.farm.database.dao.CompensationRequestDao;
import de.honoka.qqrobot.farm.database.dao.UserDao;
import de.honoka.qqrobot.farm.entity.farm.CompensationRequest;
import de.honoka.qqrobot.farm.entity.farm.User;
import de.honoka.qqrobot.farm.util.IdGenerator;
import de.honoka.qqrobot.spring.boot.starter.component.ExceptionReporter;
import de.honoka.qqrobot.spring.boot.starter.component.RobotBeanHolder;
import de.honoka.qqrobot.spring.boot.starter.property.RobotBasicProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 补偿申请
 */
@Service
public class CompensationService {

    /**
     * 清除超过一天的申请请求
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void removeOutOfDateRequest() {
        try {
            compensationRequestDao.removeOutOfDateRequest();
        } catch(Throwable t) {
            exceptionReporter.sendExceptionToDevelopingGroup(t);
            throw t;
        }
    }

    @Transactional
    public String removeRequest(int id) {
        CompensationRequest cr = compensationRequestDao
                .selectById(id);
        if(cr == null) return "没有该请求";
        compensationRequestDao.deleteById(cr);
        return "已移除该请求";
    }

    @Transactional
    public String agreeRequest(int id) {
        CompensationRequest cr = compensationRequestDao.selectById(id);
        if(cr == null) return "没有该请求";
        userDao.plusAssets(cr.getUserQq(), cr.getRequestAmount());
        compensationRequestDao.deleteById(id);
        User user = userDao.selectById(cr.getUserQq());
        robotBeanHolder.getFramework().sendPrivateMsg(user.getQq(),
                "你的补偿申请已成功，获得资金" + cr.getRequestAmount() +
                        "\n当前剩余资金：" + user.getAssets());
        return "已同意该申请";
    }

    public String queryRequests(long qq) {
        User u = userDao.selectById(qq);
        if(u == null) return StaticMessages.notRegistered;
        List<CompensationRequest> list = compensationRequestDao.selectList(
                new QueryWrapper<>(new CompensationRequest().setUserQq(qq)));
        if(list.isEmpty()) return "你还没有任何补偿申请";
        StringBuilder reply = new StringBuilder("你当前有如下未处理的补偿申请：");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for(CompensationRequest cr : list) {
            reply.append(String.format("\n%d.%s\n申请金额：%d", cr.getId(),
                    dateFormat.format(cr.getRequestTime()),
                    cr.getRequestAmount()));
        }
        return reply.toString();
    }

    public String queryRequsets() {
        List<CompensationRequest> list = compensationRequestDao
                .selectList(null);
        if(list.isEmpty()) return "当前没有任何申请";
        StringBuilder reply = new StringBuilder("当前有如下补偿申请：");
        for(CompensationRequest cr : list) {
            reply.append(String.format("\n%d.%s(%d)\n申请金额：%d", cr.getId(),
                    robotBeanHolder.getFramework().getNickOrCard(
                            null, cr.getUserQq()),
                    cr.getUserQq(), cr.getRequestAmount()));
        }
        return reply.toString();
    }

    @Transactional
    public String request(long qq, int amount) {
        User u = userDao.selectById(qq);
        if(u == null) return StaticMessages.notRegistered;
        if(amount <= 0) return "申请的金额有误";
        CompensationRequest cr = new CompensationRequest();
        Integer generatedId = IdGenerator.generateInteger(2000, id -> {
            compensationRequestDao.insert(new CompensationRequest().setId(id));
        });
        if(generatedId == null) return "现有申请较多，暂不能发起申请";
        cr.setId(generatedId).setRequestAmount(amount)
                .setRequestTime(new Date()).setUserQq(qq);
        compensationRequestDao.updateById(cr);
        robotBeanHolder.getFramework().sendGroupMsg(
                basicProperties.getDevelopingGroup(),
                String.format("新的补偿请求：\nID：%d\n%s(%d)\n申请金额：%d",
                        cr.getId(), robotBeanHolder.getFramework()
                                .getNickOrCard(null, qq),
                        qq, amount
                )
        );
        return "已成功发起申请";
    }

    @Resource
    private UserDao userDao;

    @Resource
    private ExceptionReporter exceptionReporter;

    @Resource
    private RobotBeanHolder robotBeanHolder;

    @Resource
    private RobotBasicProperties basicProperties;

    @Resource
    private CompensationRequestDao compensationRequestDao;
}
