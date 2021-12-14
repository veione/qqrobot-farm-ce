package de.honoka.qqrobot.farm.service;

import de.honoka.qqrobot.framework.Framework;
import de.honoka.qqrobot.spring.boot.starter.component.RobotAttributes;
import de.honoka.qqrobot.spring.boot.starter.property.RobotBasicProperties;
import de.honoka.util.file.FileUtils;
import de.honoka.util.various.ListRunner;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.persistence.EntityManagerFactory;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class SystemService {

    /**
     * 获取当前数据源的运行状态
     */
    @SneakyThrows
    public Map<String, Object> getDataSourceStatus() {
        Map<String, Object> map = new HashMap<>(16);
        return map;
    }

    public void sendTestMessage() {
        framework.sendGroupMsg(basicProperties.getDevelopingGroup(),
                new SimpleDateFormat("HH:mm:ss")
                        .format(new Date()) + "\n测试消息");
    }

    public void init() {
        //执行初始化器
        initer.run();
        //全部加载完成后，启动框架，开启消息处理开关
        framework.boot();
        robotAttributes.isEnabled = true;
        log.info("classpath: " + FileUtils.getClasspath());
        log.info("file.encoding: " + System.getProperty("file.encoding") +
                "\nsun.jnu.encoding: " + System.getProperty("sun.jnu.encoding"));
    }

    public void shutdown() {
        framework.stop();
        entityManagerFactory.close();
    }

    @Resource
    private RobotAttributes robotAttributes;

    @Resource
    private RobotBasicProperties basicProperties;

    @Resource
    private EntityManagerFactory entityManagerFactory;

    @Resource
    private ListRunner initer;

    @Resource
    private Framework framework;
}
