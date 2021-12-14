package de.honoka.qqrobot.farm.system;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.honoka.util.various.ListRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Timer;

/**
 * 装载一些不能直接通过注解来装载的实例（如多构造器类）
 */
@Configuration
@Component
public class SpringBeans {

    @Bean
    public Gson gson() {
        //生成便于查看的json文件的gson操作对象
        return new GsonBuilder().setDateFormat("yyyy年MM月dd日 HH:mm:ss")
                .setPrettyPrinting().create();
    }

    //初始化器，每个类可将需要在容器加载完成后进行的初始化操作存放在当中
    @Bean
    public ListRunner initalizer() {
        return new ListRunner(true);
    }

    //定时任务管理器
    @Bean
    public Timer timer() {
        return new Timer();
    }
}
