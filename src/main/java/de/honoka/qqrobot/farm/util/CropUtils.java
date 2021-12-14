package de.honoka.qqrobot.farm.util;

import de.honoka.qqrobot.farm.common.Season;
import de.honoka.qqrobot.farm.model.vo.CropVo;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CropUtils {

    //第一个大括号，声明内部类，第二个大括号，为该内部类声明动态代码块
    private static final Map<String, Integer> waterDecMap = new HashMap<>() {{
        put("晴", 20);
        put("阴", 10);
        put("多云", 15);
        put("阵雨", 5);
        put("小雨", 0);
        put("中雨", -10);
        put("大雨", -20);
        put("暴雨", -30);
        put("雨夹雪", 0);
        put("小雪", 0);
        put("中雪", -10);
        put("大雪", -20);
    }};

    private static final Map<String, Integer> qualityDecMap = new HashMap<>() {{
        put("大雨", 10);
        put("暴雨", 20);
        put("中雪", 10);
        put("大雪", 20);
    }};

    public static int getWaterDecreasement(CropVo crop) {
        String weather = crop.getLocationNowWeather();
        for(Map.Entry<String, Integer> entry : waterDecMap.entrySet()) {
            String key = entry.getKey();
            if(weather.contains(key)) return entry.getValue();
        }
        //没有找到对应的天气
        if(weather.contains("雨")) return waterDecMap.get("小雨");
        if(weather.contains("雪")) return waterDecMap.get("小雪");
        return waterDecMap.get("阴");
    }

    //只计算，不修改
    public static int getQualityDecreasement(CropVo crop) {
        int dec = 0;
        //判断天气
        Integer weatherDec = qualityDecMap.get(crop.getLocationNowWeather());
        if(weatherDec != null) dec += weatherDec;
        //判断季节
        Random ra = new Random();
        if(!crop.getTypeSeason().equals(Season.getSeason(new Date())))
            dec += ra.nextInt(9) + 1;
        //判断地形
        if(!crop.getLocationLandform().equals(crop.getTypeLandform()))
            dec += ra.nextInt(9) + 1;
        return dec;
    }
}
