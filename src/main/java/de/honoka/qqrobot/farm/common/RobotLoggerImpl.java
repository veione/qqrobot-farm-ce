package de.honoka.qqrobot.farm.common;

import de.honoka.qqrobot.farm.database.dao.ExceptionRecordDao;
import de.honoka.qqrobot.farm.database.dao.UsageLogDao;
import de.honoka.qqrobot.farm.entity.system.ExceptionRecord;
import de.honoka.qqrobot.farm.entity.system.UsageLog;
import de.honoka.qqrobot.spring.boot.starter.component.RobotBeanHolder;
import de.honoka.qqrobot.spring.boot.starter.component.logger.RobotLogger;
import de.honoka.qqrobot.spring.boot.starter.property.RobotBasicProperties;
import de.honoka.util.text.ExceptionUtils;
import de.honoka.util.various.Retrier;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.Date;

@Component
public class RobotLoggerImpl implements RobotLogger {

    @Resource
    private RobotBasicProperties basicProperties;

    @Resource
    private RobotBeanHolder robotBeanHolder;

    @Resource
    private ExceptionRecordDao exceptionRecordDao;

    @Resource
    private UsageLogDao usageLogDao;

    @Transactional
    @Override
    public void logException(Throwable t) {
        ExceptionRecord er = new ExceptionRecord();
        er.setExceptionText(ExceptionUtils.transfer(t));
        er.setDatetime(new Date());
        new Retrier().tryCode(() -> exceptionRecordDao.insert(er));
    }

    @Transactional
    @Override
    public void logMsgExecution(Long group, long qq, String msg, String reply) {
        if(reply == null) return;
        if(msg.startsWith(basicProperties.getCommandPrefix()))
            msg = msg.substring(basicProperties.getCommandPrefix().length());
        UsageLog ul = new UsageLog();
        ul.setGroupName(robotBeanHolder.getFramework().getGroupName(group));
        ul.setUsername(robotBeanHolder.getFramework().getNickOrCard(group, qq));
        ul.setQq(qq).setMsg(msg).setReply(reply).setDatetime(new Date());
        //这个对象默认会尝试3次代码块，直到成功，或失败3次
        new Retrier().tryCode(() -> usageLogDao.insert(ul));
    }
}