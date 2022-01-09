package de.honoka.qqrobot.farm.service;

import de.honoka.qqrobot.farm.common.StaticMessages;
import de.honoka.qqrobot.farm.database.dao.*;
import de.honoka.qqrobot.farm.entity.farm.*;
import de.honoka.qqrobot.farm.model.vo.CropVo;
import de.honoka.qqrobot.farm.util.CropUtils;
import de.honoka.qqrobot.farm.util.ExtendImageUtils;
import de.honoka.qqrobot.farm.util.IdGenerator;
import de.honoka.qqrobot.farm.util.MessageUtils;
import de.honoka.qqrobot.farm.web.WebConfigurations;
import de.honoka.qqrobot.spring.boot.starter.component.ExceptionReporter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static com.sobte.cqp.jcq.event.JcqApp.CC;

@Service
public class CropService {

    //public static final int LAND_SIZE = 10;		//一个区域的土地可容纳的作物数

    public static final int ERROR_MSG_LIMIT = 3;    //批量操作时，回复错误消息的最大数量

    public String getCropItemText(Crop crop) {
        String name = crop.getType();
        if(crop.isBroken()) return name + "【已损坏】";
        if(crop.isRiped()) return name + "【已成熟】";
        return name + "&nbsp;";
    }

    public String getCropImageUrl(Crop crop) {
        String baseUrl = webConf.getApplicationBaseUrl();
        if(crop.isBroken()) {
            return baseUrl + "/robot/static/img/level/broken.png";
        } else if(crop.isRiped()) {
            return baseUrl + "/robot/static/img/crop/" +
                    URLEncoder.encode(crop.getType(), StandardCharsets.UTF_8) +
                    ".png";
        } else {
            return baseUrl + "/robot/static/img/level/a" + crop.getGrowth() + ".png";
        }
    }

    public String getCrops(Long group, long qq) {
        User u = userDao.selectById(qq);
        if(u == null) return StaticMessages.notRegistered;
        String url = "http://localhost:" + webConf.getServerPort() +
                webConf.getContextPath() + "/farm/crop?province=%s&city=%s" +
                "&group=%s";
        if(group == null) group = 0L;    //URL中的group不能为null
        Location location = locationDao.selectById(u.getLocationId());
        url = String.format(url, location.getProvince(), location.getCity(),
                group);
        File img = extendImageUtils.getImage(url, 450);
        return CC.image(img.getAbsolutePath());
    }

    /**
     * 播种
     */
    @Transactional
    public String sowSeed(long qq, int seedId) {
        User u = userDao.findAndLock(qq);
        if(u == null) return StaticMessages.notRegistered;
        SeedOfUser seed = seedOfUserDao.findAndLock(seedId, qq);
        if(seed == null) return "你输入的种子编号有误";
        Location location = locationDao.selectById(u.getLocationId());
        if(cropDao.getCountOfLocation(u.getLocationId()) >= location.getLandSize())
            return "你所在的区域暂时没有可种植空间";
        //是否要收取租金
        boolean willCollectRent = location.getLandlordQq() != null &&
                !u.getQq().equals(location.getLandlordQq());
        if(willCollectRent && u.getAssets() < location.getRental()) {
            return "你的所在地需要租金" + location.getRental() +
                    "，你的资金不足，无法种植";
        }
        //生成可用ID（作物ID）
        Integer cropId = IdGenerator.generateInteger(
                location.getLandSize(), id -> {
            cropDao.insert(new Crop().setCropId(id)
                    .setLocationId(u.getLocationId()));
        });
        if(cropId == null) return "你所在的区域暂时没有可种植空间";
        //添加作物
        Crop crop = new Crop().setCropId(cropId).setLocationId(u.getLocationId())
                .setType(seed.getType());
        cropDao.update(crop);
        //移除种子
        seedOfUserDao.delete(seed);
        //更新播种时间
        u.setLastTimeSowSeed(new Date());
        //收取租金
        if(willCollectRent) {
            u.reduceAssets(location.getRental());
            userDao.plusAssets(location.getLandlordQq(), location.getRental());
        }
        userDao.updateById(u);
        return "种植成功";
    }

    public String sowSeed(long qq, int[] seedIds) {
        User u = userDao.selectById(qq);
        if(u == null) return StaticMessages.notRegistered;
        StringBuilder sowedSeed = new StringBuilder();
        List<String> errors = new ArrayList<>();
        //依次执行播种
        for(int id : seedIds) {
            try {
                String replyOfOnce = cropService.sowSeed(qq, id);
                if(replyOfOnce.contains("种植成功"))
                    sowedSeed.append(id).append(" ");
                else
                    errors.add(id + "." + replyOfOnce);
            } catch(Exception e) {
                reporter.sendExceptionToDevelopingGroup(e);
            }
        }
        String sowedSeedStr = sowedSeed.toString().trim();
        if(sowedSeedStr.equals(""))
            return MessageUtils.getMultiLineMsg(errors, ERROR_MSG_LIMIT);
        return "你成功种植了编号为" + sowedSeedStr + "的种子";
    }

    /**
     * 使所有作物进行生长
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void growAll() {
        List<Crop> crops = cropDao.selectList(null);
        for(Crop crop : crops) {
            try {
                cropService.grow(crop);
            } catch(Throwable t) {
                reporter.sendExceptionToDevelopingGroup(t);
            }
        }
    }

    /**
     * 使一个作物进行生长，根据天气和地形进行生长计算
     */
    @Transactional
    public void grow(Crop crop) {
        //锁定此条记录
        CropVo cropVo = cropDao.findVo(crop.getCropId(), crop.getLocationId(),
                true);
        crop = cropVo.toCrop();
        if(crop == null) return;
        //当前作物是否已损坏，损坏则不计算
        if(crop.isBroken()) return;
        //计算质量
        crop.reduceQuality(CropUtils.getQualityDecreasement(cropVo));
        //判断水量
        Random ra = new Random();
        if(crop.getWater() <= 0) {
            //水量少于0，不计算水量，而扣除额外的质量（35~50）
            crop.reduceQuality(ra.nextInt(16) + 35);
        } else {
            //计算水量（减少量可能为负值）
            crop.reduceWater(CropUtils.getWaterDecreasement(cropVo));
            //修正不正确的水量
            if(crop.getWater() > 100) crop.setWater(100);
            //判断水量减少后是否<=0，若是则立即减少一次质量
            if(crop.getWater() <= 0) {
                /*//余水量为负，将负值添加到质量中
                crop.growQuality += crop.water;*/
                //减少一次质量
                crop.reduceQuality(ra.nextInt(16) + 35);
                //修正不正确的水量
                crop.setWater(0);
            }
        }
        //修正不正确的质量
        if(crop.getGrowQuality() < 0) crop.setGrowQuality(0);
        //判断是否损坏
        if(!crop.isBroken()) {
            //判断是否增加生长季
            if(!crop.isRiped()) {
                long timeDistance = System.currentTimeMillis() -
                        crop.getLastTimeGrow().getTime();
                //距离上次增加生长季超过40分钟
                //这里写成39是为了防止第40分钟时判断时间距离刚好差几毫秒到40分钟
                if(timeDistance > 39 * 60 * 1000) {
                    crop.increaseGrowth();
                    crop.setLastTimeGrow(new Date());
                }
            }
        }
        //保存修改
        cropDao.update(crop);
    }

    /**
     * 浇水
     */
    @Transactional
    public String watering(long qq, int cropId) {
        User u = userDao.findAndLock(qq);
        if(u == null) return StaticMessages.notRegistered;
        if(u.getWaterRemaining() <= 0) return "你的水桶中没有水，请打水";
        Crop crop = cropDao.findAndLock(cropId, u.getLocationId());
        if(crop == null) return StaticMessages.noCrop;
        if(crop.isBroken()) return "该作物已损坏";
        //计算所需浇水量
        int waterRequirement = 100 - crop.getWater();
        if(waterRequirement <= 0) return "该作物目前不需要浇水";
        if(waterRequirement < u.getWaterRemaining()) {
            crop.setWater(100);
            u.setWaterRemaining(u.getWaterRemaining() - waterRequirement);
        } else {
            crop.addWater(u.getWaterRemaining());
            u.setWaterRemaining(0);
        }
        //保存修改
        userDao.updateById(u);
        cropDao.update(crop);
        return "浇水成功，你的水桶剩余水量为：" + u.getWaterRemaining() + "/" +
                u.getMaxWaterRemaining();
    }

    public String watering(long qq, int[] cropIds) {
        User u = userDao.selectById(qq);
        if(u == null) return StaticMessages.notRegistered;
        StringBuilder wateredCrop = new StringBuilder();
        List<String> errors = new ArrayList<>();
        //依次执行浇水
        for(int id : cropIds) {
            try {
                String replyOfOnce = cropService.watering(qq, id);
                if(replyOfOnce.contains("浇水成功"))
                    wateredCrop.append(id).append(" ");
                else {
                    errors.add(id + "." + replyOfOnce);
                    //如果没有水，则在记录本次错误后跳出，不再进行之后的操作
                    if(replyOfOnce.contains("没有水")) break;
                }
            } catch(Exception e) {
                reporter.sendExceptionToDevelopingGroup(e);
            }
        }
        String wateredCropStr = wateredCrop.toString().trim();
        //查询剩余水量
        u = userDao.selectById(qq);
        if(wateredCropStr.equals(""))
            return MessageUtils.getMultiLineMsg(errors, ERROR_MSG_LIMIT);
        return "编号为" + wateredCropStr + "的作物浇水成功，水桶剩余水量为：" +
                u.getWaterRemaining() + "/" + u.getMaxWaterRemaining();
    }

    /**
     * 收获
     */
    @Transactional
    public String harvest(long qq, int cropId) {
        User u = userDao.findAndLock(qq);
        if(u == null) return StaticMessages.notRegistered;
        Crop crop = cropDao.findAndLock(cropId, u.getLocationId());
        if(crop == null) return StaticMessages.noCrop;
        if(crop.isBroken()) return "该作物已损坏";
        if(!crop.isRiped()) return "该作物尚未成熟";
        if(System.currentTimeMillis() - u.getLastTimeSowSeed()
                .getTime() > 24 * 60 * 60 * 1000)
            return "你上次播种距离现在超过1天，请先进行播种后再收获";
        if(fruitDao.getCountOfUser(u.getQq()) >= u.getFruitBagVolume())
            return "你的果实背包已满";
        //生成可用的ID
        Integer fruitId = IdGenerator.generateInteger(
                u.getFruitBagVolume(), id -> {
            fruitDao.insert(new Fruit().setFruitId(id).setUserQq(qq));
        });
        if(fruitId == null) return "你的果实背包已满";
        //移除作物
        cropDao.delete(crop);
        //添加到用户的果实背包
        Fruit f = new Fruit().setFruitId(fruitId).setUserQq(qq)
                .setType(crop.getType());
        fruitDao.update(f);
        return "收获成功";
    }

    public String harvest(long qq, int[] cropIds) {
        User u = userDao.selectById(qq);
        if(u == null) return StaticMessages.notRegistered;
        StringBuilder harvestedCrop = new StringBuilder();
        List<String> errors = new ArrayList<>();
        //依次执行收获
        for(int id : cropIds) {
            try {
                String replyAtOnce = cropService.harvest(qq, id);
                if(replyAtOnce.contains("收获成功"))
                    harvestedCrop.append(id).append(" ");
                else
                    errors.add(id + "." + replyAtOnce);
            } catch(Exception e) {
                reporter.sendExceptionToDevelopingGroup(e);
            }
        }
        String harvestedCropStr = harvestedCrop.toString().trim();
        if(harvestedCropStr.equals(""))
            return MessageUtils.getMultiLineMsg(errors, ERROR_MSG_LIMIT);
        return "你成功收获了编号为" + harvestedCropStr + "的作物";
    }

    /**
     * 清除
     */
    @Transactional
    public String remove(long qq, int cropId) {
        User u = userDao.findAndLock(qq);
        if(u == null) return StaticMessages.notRegistered;
        Crop crop = cropDao.findAndLock(cropId, u.getLocationId());
        if(crop == null) return StaticMessages.noCrop;
        if(!crop.isBroken()) return "该作物未损坏，不能清除";
        //移除作物
        cropDao.delete(crop);
        return "清除成功";
    }

    public String remove(long qq, int[] cropIds) {
        User u = userDao.selectById(qq);
        if(u == null) return StaticMessages.notRegistered;
        StringBuilder removedCrop = new StringBuilder();
        List<String> errors = new ArrayList<>();
        //依次执行清除
        for(int id : cropIds) {
            try {
                String replyOfOnce = cropService.remove(qq, id);
                if(replyOfOnce.contains("清除成功"))
                    removedCrop.append(id).append(" ");
                else
                    errors.add(id + "." + replyOfOnce);
            } catch(Exception e) {
                reporter.sendExceptionToDevelopingGroup(e);
            }
        }
        String removedCropStr = removedCrop.toString().trim();
        if(removedCropStr.equals(""))
            return MessageUtils.getMultiLineMsg(errors, ERROR_MSG_LIMIT);
        return "你成功清除了编号为" + removedCropStr + "的作物";
    }

    @Resource
    private FruitDao fruitDao;

    @Resource
    private CropService cropService;

    @Resource
    private ExtendImageUtils extendImageUtils;

    @Resource
    private WebConfigurations webConf;

    @Resource
    private ExceptionReporter reporter;

    @Resource
    private SeedOfUserDao seedOfUserDao;

    @Resource
    private LocationDao locationDao;

    @Resource
    private UserDao userDao;

    @Resource
    private CropDao cropDao;
}
