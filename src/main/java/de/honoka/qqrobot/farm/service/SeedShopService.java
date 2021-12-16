package de.honoka.qqrobot.farm.service;

import de.honoka.qqrobot.farm.common.StaticMessages;
import de.honoka.qqrobot.farm.database.dao.LocationDao;
import de.honoka.qqrobot.farm.database.dao.SeedOfShopDao;
import de.honoka.qqrobot.farm.database.dao.SeedOfUserDao;
import de.honoka.qqrobot.farm.database.dao.UserDao;
import de.honoka.qqrobot.farm.database.service.SeedShopDbService;
import de.honoka.qqrobot.farm.entity.farm.*;
import de.honoka.qqrobot.farm.util.ExtendImageUtils;
import de.honoka.qqrobot.farm.util.IdGenerator;
import de.honoka.qqrobot.farm.util.MessageUtils;
import de.honoka.qqrobot.farm.web.WebConfigurations;
import de.honoka.qqrobot.spring.boot.starter.component.ExceptionReporter;
import de.honoka.util.various.ListRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.sobte.cqp.jcq.event.JcqApp.CC;

@Service
public class SeedShopService {

    public static final int ERROR_MSG_LIMIT = 3;    //批量操作时，回复错误消息的最大数量

    public String getShop(long qq) {
        User u = userDao.selectById(qq);
        if(u == null) return StaticMessages.notRegistered;
        String url = "http://localhost:" + webConf.getServerPort() +
                webConf.getContextPath() + "/farm/seedShop?province=%s&city=%s";
        Location location = locationDao.selectById(u.getLocationId());
        url = String.format(url, location.getProvince(), location.getCity());
        File img = extendImageUtils.getImage(url, 350);
        return CC.image(img.getAbsolutePath());
    }

    /**
     * 刷新所有地区的种子商店
     */
    @Scheduled(cron = "0 0 0/1 * * ?")
    public synchronized void updateAllShop() {
        //查询所有地区
        List<Integer> idList = locationDao.findAllId();
        //更新所有地区
        for(Integer id : idList) {
            try {
                seedShopService.updateShop(id);
            } catch(Exception e) {
                reporter.sendExceptionToDevelopingGroup(e);
            }
        }
    }

    /**
     * 刷新一个地区的种子商店
     */
    @Transactional
    public void updateShop(Integer locationId) {
        Location location = locationDao.findAndLock(locationId);
        //先生成新的列表
        List<SeedOfShop> seeds = new ArrayList<>();
        List<CropType> randomTypes = cropTypeService.randomType(
                location.getLandform(), location.getShopVolume());
        Random ra = new Random();
        for(int i = 0; i < location.getShopVolume(); i++) {
            SeedOfShop seed = new SeedOfShop();
            seed.setSeedId(i + 1);
            seed.setLocationId(locationId);
            seed.setType(randomTypes.get(i).getName());
            //波动价格
            int price = randomTypes.get(i).getSeedPrice();
            int wave = (int) (price * 0.1);
            seed.setPrice(price + (ra.nextInt(wave * 2 + 1) - wave));
            seeds.add(seed);
        }
        //查找并删除现有种子
        seedOfShopDao.deleteAllSeedOfShop(locationId);
        //将新列表中的所有内容添加到数据库中
        seedShopDbService.saveBatch(seeds);
    }

    /**
     * 用户请求购买种子
     */
    @Transactional
    public String buySeed(long qq, int seedId) {
        User u = userDao.findAndLock(qq);
        if(u == null) return StaticMessages.notRegistered;
        SeedOfShop seed = seedOfShopDao.findAndLock(seedId, u.getLocationId());
        if(seed == null) return "你输入的种子编号有误";
        if(u.getAssets() < seed.getPrice()) return "你的资金不足";
        if(seedOfUserDao.getCountOfUser(qq) >= u.getSeedBagVolume())
            return "你的种子背包已满";
        //生成可用的ID（种子背包中的种子的ID）
        Integer seedIdInBag = IdGenerator.generateInteger(
                u.getSeedBagVolume(), id -> {
            seedOfUserDao.insert(new SeedOfUser().setSeedId(id).setUserQq(qq));
        });
        if(seedIdInBag == null) return "你的种子背包已满";
        //从商店移除
        seedOfShopDao.delete(seed);
        //添加到用户的种子背包
        SeedOfUser seedOfUser = new SeedOfUser();
        seedOfUser.setSeedId(seedIdInBag);
        seedOfUser.setType(seed.getType());
        seedOfUser.setUserQq(qq);
        seedOfUserDao.update(seedOfUser);
        //扣款
        u.reduceAssets(seed.getPrice());
        userDao.updateById(u);
        return "购买成功，你的资金余额为：" + u.getAssets();
    }

    public String buySeed(long qq, int[] seedIds) {
        User u = userDao.selectById(qq);
        if(u == null) return StaticMessages.notRegistered;
        StringBuilder boughtSeed = new StringBuilder();        //已购买的种子编号
        List<String> errors = new ArrayList<>();
        //依次执行购买
        for(int id : seedIds) {
            try {
                String replyOfOnce = seedShopService.buySeed(qq, id);
                if(replyOfOnce.contains("购买成功")) {
                    boughtSeed.append(id).append(" ");
                    continue;
                }
                errors.add(id + "." + replyOfOnce);
                if(replyOfOnce.contains("资金不足") || replyOfOnce
                        .contains("背包已满")) break;
            } catch(Exception e) {
                reporter.sendExceptionToDevelopingGroup(e);
            }
        }
        String boughtSeedStr = boughtSeed.toString().trim();
        //查询购买后资金
        u = userDao.selectById(qq);
        if(boughtSeedStr.equals(""))
            return MessageUtils.getMultiLineMsg(errors, ERROR_MSG_LIMIT);
        return "你成功购买了编号为" + boughtSeedStr +
                "的种子，资金余额为：" + u.getAssets();
    }

    public SeedShopService(ListRunner initer) {
        initer.add(this::updateAllShop, 3);
    }

    @Resource
    private SeedShopDbService seedShopDbService;

    @Resource
    private SeedOfUserDao seedOfUserDao;

    @Resource
    private SeedOfShopDao seedOfShopDao;

    @Resource
    private CropTypeService cropTypeService;

    @Resource
    private SeedShopService seedShopService;

    @Resource
    private LocationDao locationDao;

    @Resource
    private ExtendImageUtils extendImageUtils;

    @Resource
    private WebConfigurations webConf;

    @Resource
    private ExceptionReporter reporter;

    @Resource
    private UserDao userDao;
}
