package de.honoka.qqrobot.farm.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import de.honoka.qqrobot.farm.database.dao.*;
import de.honoka.qqrobot.farm.entity.farm.*;
import de.honoka.qqrobot.farm.service.CropService;
import de.honoka.qqrobot.farm.service.CropTypeService;
import de.honoka.qqrobot.farm.web.WebConfigurations;
import de.honoka.qqrobot.framework.Framework;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/farm")
public class FarmController {

    @RequestMapping("/fruitBag")
    public ModelAndView fruitBag(@RequestParam long qq) {
        User u = userDao.selectById(qq);
        List<Fruit> fruits = fruitDao.selectList(new LambdaQueryWrapper<Fruit>()
                .eq(Fruit::getUserQq, qq).orderByAsc(Fruit::getFruitId));
        Map<String, CropType> nameTypeMap = cropTypeService.getNameTypeMap(fruits
                .stream().map(Fruit::getType).collect(Collectors.toList()));
        ModelAndView mav = new ModelAndView("farm/fruitBag");
        mav.addObject("fruits", fruits);
        mav.addObject("nameTypeMap", nameTypeMap);
        mav.addObject("user", u);
        commonAddObject(mav);
        return mav;
    }

    @RequestMapping("/crop")
    public ModelAndView crop(@RequestParam Long group,
            @RequestParam String province, @RequestParam String city) {
        Location location = locationDao.findByProvinceAndCity(province, city);
        List<Crop> crops = cropDao.selectList(new LambdaQueryWrapper<Crop>()
                .eq(Crop::getLocationId, location.getLocationId())
                .orderByAsc(Crop::getCropId));
        ModelAndView mav = new ModelAndView("farm/crop");
        mav.addObject("crops", crops);
        mav.addObject("location", location);
        mav.addObject("cropService", cropService);
        if(location.getLandlordQq() != null) {
            mav.addObject("landlordName",
                    framework.getNickOrCard(group, location.getLandlordQq()));
        } else {
            mav.addObject("landlordName", null);
        }
        commonAddObject(mav);
        return mav;
    }

    @RequestMapping("/seedBag")
    public ModelAndView seedBag(@RequestParam long qq) {
        User u = userDao.selectById(qq);
        List<SeedOfUser> seeds = seedOfUserDao.selectList(
                new LambdaQueryWrapper<SeedOfUser>()
                        .eq(SeedOfUser::getUserQq, qq)
                        .orderByAsc(SeedOfUser::getSeedId)
        );
        Map<String, CropType> nameTypeMap = cropTypeService.getNameTypeMap(seeds
                .stream().map(SeedOfUser::getType).collect(Collectors.toList()));
        ModelAndView mav = new ModelAndView("farm/seedBag");
        mav.addObject("seeds", seeds);
        mav.addObject("nameTypeMap", nameTypeMap);
        mav.addObject("user", u);
        commonAddObject(mav);
        return mav;
    }

    @RequestMapping("/seedShop")
    public ModelAndView seedShop(
            @RequestParam String province, @RequestParam String city) {
        Location location = locationDao.findByProvinceAndCity(province, city);
        List<SeedOfShop> seeds = seedOfShopDao.selectList(
                new LambdaQueryWrapper<SeedOfShop>()
                        .eq(SeedOfShop::getLocationId, location.getLocationId())
                        .orderByAsc(SeedOfShop::getSeedId)
        );
        Map<String, CropType> nameTypeMap = cropTypeService.getNameTypeMap(seeds
                .stream().map(SeedOfShop::getType).collect(Collectors.toList()));
        ModelAndView mav = new ModelAndView("farm/seedShop");
        mav.addObject("seeds", seeds);
        mav.addObject("nameTypeMap", nameTypeMap);
        mav.addObject("location", location);
        commonAddObject(mav);
        return mav;
    }

    private void commonAddObject(ModelAndView mav) {
        mav.addObject("baseUrl", webConf.getApplicationBaseUrl());
        mav.addObject("encoder", encoder);
    }

    private static class Encoder {

        public String encode(String str) {
            return URLEncoder.encode(str, StandardCharsets.UTF_8);
        }
    }

    private final Encoder encoder = new Encoder();

    @Resource
    private CropTypeService cropTypeService;

    @Resource
    private SeedOfShopDao seedOfShopDao;

    @Resource
    private SeedOfUserDao seedOfUserDao;

    @Resource
    private CropDao cropDao;

    @Resource
    private LocationDao locationDao;

    @Resource
    private FruitDao fruitDao;

    @Resource
    private Framework framework;

    @Resource
    private CropService cropService;

    @Resource
    private WebConfigurations webConf;

    @Resource
    private UserDao userDao;
}
