package de.honoka.qqrobot.farm.util.train.ticket;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.honoka.qqrobot.farm.entity.farm.TrainTicket;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Component
public class QunarTrainTicketUtils extends TrainTicketUtils {

    @Override
    public List<TrainTicket> getTickets(String from, String to, int count) {
        //获取日期
        Date date = new Date();
        //获取仅含日期的字符串
        String dateStr = getDateOnly(date);
        //获取json，提取车票
        JsonObject json = getTicketsJson(from, to, dateStr);
        List<TrainTicket> tickets = parseTickets(json, count);
        //判断数量是否足够
        if(tickets.size() < count) {
            //不足够，获取下一天的车票
            date = new Date(date.getTime() + 24 * 60 * 60 * 1000);
            dateStr = getDateOnly(date);
            json = getTicketsJson(from, to, dateStr);
            //获取剩余数量的车票
            List<TrainTicket> nextDayTickets = parseTickets(json,
                    count - tickets.size());
            //添加剩余部分的车票到列表
            tickets.addAll(nextDayTickets);
        }
        return tickets;
    }

    private JsonObject getTicketsJson(String from, String to, String dateStr) {
        String url = "https://train.qunar.com/dict/open/s2s.do?dptStation=%s&arrStation=%s&date=%s&type=normal&user=neibu&source=site&start=1&num=500&sort=3";
        url = String.format(url, from, to, dateStr);
        final int RETRY_TIMES = 3;
        //链接有时可能无法请求到数据，故多次尝试
        for(int i = 0; i < RETRY_TIMES; i++) {
            String jsonStr;
            try {
                jsonStr = Jsoup.connect(url).ignoreContentType(true)
                        .timeout(5 * 1000).get().text();
                JsonObject json = JsonParser.parseString(jsonStr).getAsJsonObject();
                boolean ret = json.get("ret").getAsBoolean();
                if(ret) return json;
            } catch(Exception e) {
                if(i == RETRY_TIMES - 1) e.printStackTrace();
            }
        }
        throw new RuntimeException("获取车票数据时网络请求失败");
    }

    private List<TrainTicket> parseTickets(JsonObject json, int count) {
        JsonArray s2sBeanList = json.get("data").getAsJsonObject()
                .get("s2sBeanList").getAsJsonArray();
        List<TrainTicket> tickets = new ArrayList<>();
        Random ra = new Random();
        for(JsonElement je : s2sBeanList) {
            JsonObject ticketJson = je.getAsJsonObject();
            JsonObject extraBeanMap = ticketJson.get("extraBeanMap").getAsJsonObject();
            JsonObject seats = ticketJson.get("seats").getAsJsonObject();
            TrainTicket ticket = new TrainTicket();
            //计算虚拟价格
            double priceSum = 0;
            int seatTypeCount = 0;
            for(String key : seats.keySet()) {
                JsonObject seat = seats.get(key).getAsJsonObject();
                if(seat.get("count").getAsInt() <= 0) continue;
                priceSum += seat.get("price").getAsDouble();
                seatTypeCount++;
            }
            if(seatTypeCount <= 0) continue;    //此车票没有可用的座位类型，不添加
            ticket.setPrice(getVirtualPrice((int) (priceSum / seatTypeCount), ra));
            //获取其他属性
            ticket.setTrainNo(ticketJson.get("trainNo").getAsString());
            ticket.setFromStation(ticketJson.get("dptStationName").getAsString());
            ticket.setToStation(ticketJson.get("arrStationName").getAsString());
            ticket.setStartTime(getDate(extraBeanMap.get("dptDate").getAsString(),
                    ticketJson.get("dptTime").getAsString()));
            //判断是否已经过了开车时间，若是则跳过此条记录
            if(System.currentTimeMillis() > ticket.getStartTime().getTime())
                continue;
            ticket.setArriveTime(getDate(extraBeanMap.get("arrDate").getAsString(),
                    ticketJson.get("arrTime").getAsString()));
            tickets.add(ticket);
            //添加后判断数量是否足够
            if(tickets.size() >= count) break;
        }
        return tickets;
    }
}
