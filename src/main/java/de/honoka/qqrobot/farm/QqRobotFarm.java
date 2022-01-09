package de.honoka.qqrobot.farm;

import de.honoka.qqrobot.farm.service.SystemService;
import de.honoka.qqrobot.farm.system.SystemComponents;
import de.honoka.qqrobot.framework.Framework;
import de.honoka.qqrobot.spring.boot.starter.component.RobotAttributes;
import de.honoka.standard.system.ConsoleWindow;
import de.honoka.util.file.FileUtils;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

@SpringBootApplication
public class QqRobotFarm {

    private static SystemService systemService;

    public static void main(String[] args) {
        //region 初始化窗口与托盘图标
        ConsoleWindow console = new ConsoleWindow("QQ Robot Farm",
                null, QqRobotFarm::exit);
        console.setAutoScroll(true);
        console.setScreenZoomScale(1.25);
        console.show();
        //endregion
        //region 构建应用、加载配置、启动应用
        checkAndOutputFiles();
        SpringApplication app = new SpringApplication(QqRobotFarm.class);
        app.run(args);
        //endregion
        //region 装配组件
        ApplicationContext context = SystemComponents.applicationContext;
        RobotAttributes attributes = context.getBean(RobotAttributes.class);
        attributes.consoleWindow = console;
        systemService = context.getBean(SystemService.class);
        systemService.init();
        //添加托盘图标菜单项
        console.addTrayIconMenuItem("Relogin", true,
                context.getBean(Framework.class)::reboot);
        console.addTrayIconMenuItem("Send Test Message", false,
                systemService::sendTestMessage);
        //endregion
    }

    public static void exit() {
        systemService.shutdown();
    }

    public static void checkAndOutputFiles() {
        String classpath = FileUtils.getClasspath();
        Class<?> thisClass = QqRobotFarm.class;
        //deviceInfo.json
        File deviceInfo = new File(Path.of(classpath, "qqrobot",
                "mirai", "deviceInfo.json").toString());
        if(!deviceInfo.exists()) {
            URL url = thisClass.getResource("/qqrobot/mirai" +
                    "/deviceInfo.json");
            if(url != null) {
                FileUtils.urlToFile(url, deviceInfo);
            }
        }
        //crop_types.csv
        File cropTypes = new File(Path.of(classpath, "farm",
                "crop_types.csv").toString());
        if(!cropTypes.exists()) {
            FileUtils.urlToFile(Objects.requireNonNull(thisClass.getResource(
                    "/farm/crop_types.csv")), cropTypes);
        }
    }

    public static Properties loadYmlProperties(String path) {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource(path));
        return yaml.getObject();
    }
}
