package de.honoka.qqrobot.farm.util.train.ticket;

import de.honoka.qqrobot.farm.entity.farm.TrainTicket;
import lombok.SneakyThrows;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

public abstract class TrainTicketUtils {

    /**
     * 提供始发地名与到达地名，以及数量限制，获取车票列表
     */
    public abstract List<TrainTicket> getTickets(String from, String to, int count);

    /**
     * 日期时间转为不含年份与秒数的字符串
     */
    public static String getDisplayTimeStr(Date time) {
        DateFormat dateTimeFormat = new SimpleDateFormat("MM-dd HH:mm");
        return dateTimeFormat.format(time);
    }

    /**
     * 只提取日期字符串
     */
    public static String getDateOnly(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(date);
    }

    /**
     * 根据发车时间和历时长度字符串，计算出到达时间的Date日期
     */
    public static Date getArriveTime(Date startTime, String hourAndMin) {
        int needHour, needMinute;
        String[] parts = hourAndMin.split(":");
        needHour = Integer.parseInt(parts[0]);
        needMinute = Integer.parseInt(parts[1]);
        long arriveTimeMillis = startTime.getTime();
        arriveTimeMillis += needHour * 60 * 60 * 1000;
        arriveTimeMillis += needMinute * 60 * 1000;
        return new Date(arriveTimeMillis);
    }

    /**
     * 计算虚拟价格
     */
    public static int getVirtualPrice(int price, Random ra) {
        return price * 20 + ra.nextInt(10);
    }

    /**
     * 将日期、时和分字符串组装成Date日期
     */
    @SneakyThrows
    public static Date getDate(String date, String hourAndMin) {
        DateFormat timeStampFormat =
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return timeStampFormat.parse(date + " " + hourAndMin + ":00");
    }
}
