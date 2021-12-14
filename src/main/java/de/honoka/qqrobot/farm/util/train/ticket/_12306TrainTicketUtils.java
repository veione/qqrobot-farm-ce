package de.honoka.qqrobot.farm.util.train.ticket;

import com.google.gson.*;
import de.honoka.qqrobot.farm.entity.farm.TrainTicket;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;

import java.util.*;

//@Component
public class _12306TrainTicketUtils extends TrainTicketUtils {

    private final List<String> API_SUFFIXES = Arrays.asList(
            "", "A", "O", "Q", "R", "T", "X", "Y", "Z"
    );

    private int nowApiSuffixIndex = 7;

    /**
     * 更新为指定的后缀
     */
    private void changeApiSuffix(String suffix) {
        if(!API_SUFFIXES.contains(suffix)) {
            API_SUFFIXES.add(suffix);
        }
        String nowSuffix = API_SUFFIXES.get(nowApiSuffixIndex);
        int times = 0;
        while(!nowSuffix.equals(suffix)) {
            changeApiSuffix();
            //防止无限循环
            times++;
            if(times > API_SUFFIXES.size()) break;
        }
    }

    /**
     * 更新API后缀
     */
    private void changeApiSuffix() {
        nowApiSuffixIndex++;
        if(nowApiSuffixIndex >= API_SUFFIXES.size())
            nowApiSuffixIndex = 0;
    }

    /**
     * 根据起始城市和到达城市查询可用的车票
     */
    @Override
    public List<TrainTicket> getTickets(String from, String to, int count) {
        //获取日期
        Date date = new Date();
        //获取仅含日期的字符串
        String dateStr = getDateOnly(date);
        //获取城市名缩写
        String fromAbbr = getStaionAbbr(from);
        String toAbbr = getStaionAbbr(to);
        //获取Cookie
        Map<String, String> cookies = getCookies(fromAbbr, toAbbr, dateStr);
        //获取json，提取车票
        JsonObject json = getTicketsJson(fromAbbr, toAbbr, dateStr, cookies);
        List<TrainTicket> tickets = parseTickets(json, dateStr, cookies, count);
        //判断数量是否足够
        if(tickets.size() < count) {
            //不足够，获取下一天的车票
            date = new Date(date.getTime() + 24 * 60 * 60 * 1000);
            dateStr = getDateOnly(date);
            json = getTicketsJson(fromAbbr, toAbbr, dateStr, cookies);
            //获取剩余数量的车票
            List<TrainTicket> nextDayTickets = parseTickets(json, dateStr,
                    cookies, count - tickets.size());
            //添加剩余部分的车票到列表
            tickets.addAll(nextDayTickets);
        }
        return tickets;
    }

    /**
     * 解析json数据，提取车票信息
     */
    public List<TrainTicket> parseTickets(JsonObject json, String dateStr,
            Map<String, String> cookies, int count) {
        //获取存储车票信息的数组
        JsonArray result = json.getAsJsonObject("data")
                .getAsJsonArray("result");
        //获取解析车站名称的map
        JsonObject map = json.getAsJsonObject("data").getAsJsonObject("map");
        //解析每一个车票信息
        List<TrainTicket> tickets = new ArrayList<>();
        Random ra = new Random();
        for(JsonElement je : result) {
            String row = je.getAsString();
            String[] cols = row.split("\\|");
            //每个数据在提取过程中均可能加载失败，故遇到有问题的数据则跳过
            try {
                TrainTicket ticket = new TrainTicket();
                ticket.setTrainNo(cols[3]);
                ticket.setFromStation(getStationName(cols[6], map));
                ticket.setToStation(getStationName(cols[7], map));
                ticket.setStartTime(getDate(dateStr, cols[8]));
                ticket.setArriveTime(getArriveTime(ticket.getStartTime(), cols[10]));
                //获取虚拟价格
                ticket.setPrice(getVirtualPrice(getTicketPrice(cols[2], cols[16],
                        cols[17], cols[35], dateStr, cookies), ra));
                //判断是否已经过了开车时间，若是则跳过此条记录
                if(System.currentTimeMillis() > ticket.getStartTime().getTime())
                    continue;
                tickets.add(ticket);
            } catch(Exception e) {
                //不处理，跳过添加此行
            }
            //添加后判断数量是否足够
            if(tickets.size() >= count) break;
        }
        return tickets;
    }

    /**
     * 获取指定始发站指定日期的车票数据
     */
    public JsonObject getTicketsJson(
            String fromAbbr, String toAbbr, String dateStr,
            Map<String, String> cookies) {
        final int RETRY_TIMES = 3;
        //链接有时可能无法请求到数据，故多次尝试
        for(int i = 0; i < RETRY_TIMES; i++) {
            //请求API，得到json
            String url = "https://kyfw.12306.cn/otn/leftTicket/query%s?leftTicketDTO.train_date=%s&leftTicketDTO.from_station=%s&leftTicketDTO.to_station=%s&purpose_codes=ADULT";
            url = String.format(url, API_SUFFIXES.get(nowApiSuffixIndex),
                    dateStr, fromAbbr, toAbbr);
            String jsonStr;
            try {
                jsonStr = Jsoup.connect(url).cookies(cookies)
                        .ignoreContentType(true).timeout(5 * 1000).get().text();
            } catch(Exception e) {
                continue;
            }
            //判断数据是否正确
            //非json数据
            if(jsonStr.contains("网络可能存在问题")) {
                //更新cookie后再试
                cookies = getRandomCookies();
                continue;
            }
            //json数据
            JsonObject json = JsonParser.parseString(jsonStr).getAsJsonObject();
            boolean status = json.get("status").getAsBoolean();
            if(!status) {
                //json数据中状态为未成功
                String cUrl = json.get("c_url").getAsString();
                String suffix = cUrl.substring(cUrl.length() - 1);
                //更换为指定的后缀再试
                changeApiSuffix(suffix);
                continue;
            }
            //数据正确
            return json;
        }
        //多次尝试均不能获取到正确的数据，则更换后缀
        changeApiSuffix();
        throw new RuntimeException("获取车票数据时网络请求失败");
    }

    @SneakyThrows
    public Map<String, String> getCookies(
            String fromAbbr, String toAbbr, String dateStr) {
        String url = "https://kyfw.12306.cn/otn/leftTicket/init?linktypeid=dc&fs=,%s&ts=,%s&date=%s&flag=N,N,Y";
        url = String.format(url, fromAbbr, toAbbr, dateStr);
        return Jsoup.connect(url).execute().cookies();
    }

    public Map<String, String> getRandomCookies() {
        return getCookies(getStaionAbbr("北京"), getStaionAbbr("上海"),
                getDateOnly(new Date()));
    }

    /**
     * 查询指定车号、指定始发站的到站的车票价格（人民币）
     */
    @SneakyThrows
    public int getTicketPrice(String trainNo, String fromStationNo,
                              String toStationNo, String seatTypes, String date,
                              Map<String, String> cookies) {
        String url = "https://kyfw.12306.cn/otn/leftTicket/queryTicketPrice?train_no=%s&from_station_no=%s&to_station_no=%s&seat_types=%s&train_date=%s";
        url = String.format(url, trainNo, fromStationNo, toStationNo,
                seatTypes, date);
        //此链接有时可能无法请求到数据，故多次尝试
        String jsonStr = null;
        for(int i = 0; i < 3; i++) {
            jsonStr = Jsoup.connect(url).cookies(cookies).ignoreContentType(true)
                    .get().text();
            //判断数据是否正确
            if(!jsonStr.contains("网络可能存在问题")) break;
            //更新cookie再试
            cookies = getRandomCookies();
        }
        JsonObject json = JsonParser.parseString(jsonStr).getAsJsonObject();
        JsonObject data = json.getAsJsonObject("data");
        int typeCount = 0;
        double price = 0;
        for(String key : data.keySet()) {
            JsonElement je = data.get(key);
            if(!(je instanceof JsonPrimitive)) continue;
            String priceStr = je.getAsString();
            if(!priceStr.contains("¥")) continue;
            typeCount++;
            price += Double.parseDouble(priceStr.substring(1));
        }
        //检查整数价格
        int intPrice = (int) (price / typeCount);
        if(intPrice <= 0) throw new Exception("查询到的票价有误：" + intPrice);
        return intPrice;
    }

    public String getStationName(String abbr, JsonObject map) {
        return map.get(abbr).getAsString();
    }

    @SneakyThrows
    public String getStaionAbbr(String city) {
        if(stations == null) {
            String url = "https://kyfw.12306.cn/otn/resources/js/framework/station_name.js";
            String text = Jsoup.connect(url).ignoreContentType(true).get().text();
            text = text.substring(text.indexOf("'") + 1);
            text = text.substring(0, text.indexOf("'"));
            stations = Arrays.asList(text.split("@"));
            stations = stations.subList(1, stations.size());
        }
        //精确匹配
        for(String station : stations) {
            String[] parts = station.split("\\|");
            if(parts[1].equals(city)) return parts[2];
        }
        //没有精确匹配，进行模糊匹配（一般不会出现）
        for(String station : stations) {
            String[] parts = station.split("\\|");
            if(parts[1].contains(city)) return parts[2];
        }
        return null;
    }

    private List<String> stations;
}
