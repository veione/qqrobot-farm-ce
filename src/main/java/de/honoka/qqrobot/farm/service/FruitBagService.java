package de.honoka.qqrobot.farm.service;

import de.honoka.qqrobot.farm.common.Season;
import de.honoka.qqrobot.farm.common.StaticMessages;
import de.honoka.qqrobot.farm.database.dao.CropTypeDao;
import de.honoka.qqrobot.farm.database.dao.FruitDao;
import de.honoka.qqrobot.farm.database.dao.LocationDao;
import de.honoka.qqrobot.farm.database.dao.UserDao;
import de.honoka.qqrobot.farm.entity.farm.CropType;
import de.honoka.qqrobot.farm.entity.farm.Fruit;
import de.honoka.qqrobot.farm.entity.farm.Location;
import de.honoka.qqrobot.farm.entity.farm.User;
import de.honoka.qqrobot.farm.util.ExtendImageUtils;
import de.honoka.qqrobot.farm.web.WebConfigurations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.io.File;
import java.util.Date;

import static com.sobte.cqp.jcq.event.JcqApp.CC;

@Service
public class FruitBagService {

    //public static final int BAG_VOLUME = 10;

    public String getBag(long qq) {
        User u = userDao.selectById(qq);
        if(u == null) return StaticMessages.notRegistered;
        String url = "http://localhost:" + webConf.getServerPort() +
                webConf.getContextPath() + "/farm/fruitBag?qq=%s";
        url = String.format(url, qq);
        File img = extendImageUtils.getImage(url);
        return CC.image(img.getAbsolutePath());
    }

    /**
     * 卖出
     */
    @Transactional
    public String sell(long qq, int fruitId) {
        User u = userDao.findAndLock(qq);
        if(u == null) return StaticMessages.notRegistered;
        Fruit f = fruitDao.findAndLock(fruitId, qq);
        if(f == null) return StaticMessages.noFruit;
        //计算果实价格
        boolean differentLandform = false, differentSeason = false;
        CropType type = cropTypeDao.selectById(f.getType());
        int fruitPrice = type.getFruitPrice();
        //判断所在地地形与果实适合生长的地形是否不同
        Location location = locationDao.selectById(u.getLocationId());
        if(!location.getLandform().equals(type.getLandform())) {
            differentLandform = true;
            fruitPrice *= 1.5;
        }
        //判断季节是否不同
        if(!Season.getSeason(new Date()).equals(type.getSeason())) {
            differentSeason = true;
            fruitPrice *= 1.5;
        }
        //移除果实
        fruitDao.delete(f);
        //增加资金
        userDao.plusAssets(qq, fruitPrice);
        String reply = "";
        if(differentLandform)
            reply += "该果实适合生长的地形与当前地形不同，售价+50%\n";
        if(differentSeason)
            reply += "该果实适合生长的季节与当前季节不同，售价+50%\n";
        reply += "卖出成功，获得资金" + fruitPrice;
        return reply;
    }

    public String sell(long qq, int[] fruitIds) {
        User u = userDao.selectById(qq);
        if(u == null) return StaticMessages.notRegistered;
        StringBuilder soldFruit = new StringBuilder();
        String replyOfOnce = null;
        int priceCount = 0;
        //依次执行卖出
        for(int id : fruitIds) {
            replyOfOnce = fruitBagService.sell(qq, id);
            if(replyOfOnce.contains("卖出成功")) {
                String priceStr = replyOfOnce.substring(replyOfOnce
                        .indexOf("获得资金") + "获得资金".length());
                priceCount += Integer.parseInt(priceStr);
                soldFruit.append(id).append(" ");
            }
        }
        String soldFruitStr = soldFruit.toString().trim();
        if(soldFruitStr.equals("")) return replyOfOnce;
        return "你成功卖出了编号为" + soldFruitStr + "的作物，获得资金" + priceCount;
    }

    @Resource
    private FruitBagService fruitBagService;

    @Resource
    private LocationDao locationDao;

    @Resource
    private CropTypeDao cropTypeDao;

    @Resource
    private ExtendImageUtils extendImageUtils;

    @Resource
    private WebConfigurations webConf;

    @Resource
    private FruitDao fruitDao;

    @Resource
    private UserDao userDao;
}
