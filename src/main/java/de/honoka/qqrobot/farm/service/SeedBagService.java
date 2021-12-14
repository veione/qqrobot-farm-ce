package de.honoka.qqrobot.farm.service;

import de.honoka.qqrobot.farm.common.StaticMessages;
import de.honoka.qqrobot.farm.database.dao.UserDao;
import de.honoka.qqrobot.farm.entity.farm.User;
import de.honoka.qqrobot.farm.util.ExtendImageUtils;
import de.honoka.qqrobot.farm.web.WebConfigurations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;

import static com.sobte.cqp.jcq.event.JcqApp.CC;

@Service
public class SeedBagService {

    //public static final int BAG_VOLUME = 10;		//种子背包的容量

    public String getBag(long qq) {
        User u = userDao.selectById(qq);
        if(u == null) return StaticMessages.notRegistered;
        String url = "http://localhost:" + webConf.getServerPort() +
                webConf.getContextPath() + "/farm/seedBag?qq=%s";
        url = String.format(url, qq);
        File img = extendImageUtils.getImage(url);
        return CC.image(img.getAbsolutePath());
    }

    @Resource
    private ExtendImageUtils extendImageUtils;

    @Resource
    private WebConfigurations webConf;

    @Resource
    private UserDao userDao;
}
