package de.honoka.qqrobot.farm.common;

import java.util.Calendar;
import java.util.Date;

public class Season {

    public static final String
            SPRING = "春",
            SUMMER = "夏",
            AUTUMN = "秋",
            WINTER = "冬";

    public static String getSeason(int month) {
        switch(month) {
            case 3:
            case 4:
            case 5:
                return SPRING;
            case 6:
            case 7:
            case 8:
                return SUMMER;
            case 9:
            case 10:
            case 11:
                return AUTUMN;
            case 12:
            case 1:
            case 2:
                return WINTER;
            default:
                return null;
        }
    }

    public static String getSeason(Date d) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);
        return getSeason(calendar.get(Calendar.MONTH) + 1);
    }

    public static String getNextSeason(String season) {
        switch(season) {
            case SPRING:
                return SUMMER;
            case SUMMER:
                return AUTUMN;
            case AUTUMN:
                return WINTER;
            case WINTER:
                return SPRING;
            default:
                return null;
        }
    }
}
