package de.honoka.qqrobot.farm.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.honoka.qqrobot.farm.common.Landform;
import de.honoka.qqrobot.farm.entity.farm.Location;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 分析地理位置的有关的工具
 */
public class LocationUtils {

    /**
     * 根据传入的省、市信息封装出一个可用的Location实例
     */
    public static Location newLocation(String province, String city) {
        Location location = new Location();
        location.setProvince(province);
        location.setCity(city);
        try {
            location.setLandform(LocationUtils.getLandform(location.getCity()));
            location.setWeatherUrl(LocationUtils.getWeatherUrl(
                    location.getProvince(), location.getCity()));
        } catch(Exception e) {
            //none
        }
        //无法获取到此位置的天气或地形
        if(location.getLandform() == null || location.getWeatherUrl() == null)
            return null;
        location.updateWeather();
        return location;
    }

    /**
     * 获取指定省、市的主要地形
     */
    @SneakyThrows
    public static String getLandform(String city) {
        if(coastCitys.contains(city)) return Landform.COAST;
        if(hillyCitys.contains(city)) return Landform.HILLY_AREA;
        String urlBase = "https://www.chahaiba.com";
        String url = urlBase + "/s?wd=" + city;
        Document doc = Jsoup.connect(url).timeout(20 * 1000).get();
        //取该市几个地点的海拔，计算平均海拔和海拔极差
        int heightSum = 0, avgHeight, heightDifference;
        List<Integer> heights = new ArrayList<>();    //用于计算海拔极差
        //搜索结果，若提供的地址有误，此集合将为空集合
        Elements placeTags = doc.selectFirst("dl.chb-panel.chb-list-one")
                .select("dd");
        final int PLACE_COUNT = Math.min(placeTags.size(), 5);    //最多5个地点
        for(int i = 0; i < PLACE_COUNT; i++) {
            url = urlBase + placeTags.get(i).selectFirst("a")
                    .attr("href");
            doc = Jsoup.connect(url).timeout(20 * 1000).get();
            String heightText = doc.selectFirst("div.layui-container.chb-container")
                    .selectFirst("div.layui-row.layui-col-space15")
                    .selectFirst("div.layui-col-md8")
                    .selectFirst("div.layui-card")
                    .selectFirst("div.layui-card-body")
                    .selectFirst("ul > li").text();
            heightText = heightText.split("：")[1];
            heightText = heightText.substring(0, heightText.length() - 1);
            int height = Integer.parseInt(heightText);
            heightSum += height;
            heights.add(height);
        }
        avgHeight = heightSum / PLACE_COUNT;
        heightDifference = Collections.max(heights) - Collections.min(heights);
        if(avgHeight > 700 || heightDifference > 400)
            return Landform.HILLY_AREA;
        return Landform.PLAIN;
    }

    /**
     * 根据省、市名称查询获取该位置天气的URL
     */
    @SneakyThrows
    public static String getWeatherUrl(String province, String city) {
        final String[] urls = {
                "http://www.weather.com.cn/textFC/hb.shtml",
                "http://www.weather.com.cn/textFC/db.shtml",
                "http://www.weather.com.cn/textFC/hd.shtml",
                "http://www.weather.com.cn/textFC/hz.shtml",
                "http://www.weather.com.cn/textFC/hn.shtml",
                "http://www.weather.com.cn/textFC/xb.shtml",
                "http://www.weather.com.cn/textFC/xn.shtml",
                "http://www.weather.com.cn/textFC/gat.shtml"
        };
        for(String url : urls) {
            Document doc = Jsoup.connect(url).timeout(20 * 1000).get();
            Elements provinceTags = doc.selectFirst("div.hanml > div.conMidtab")
                    .select("div > table > tbody");
            for(Element provinceTag : provinceTags) {
                Elements cityTags = provinceTag.select("tr");
                String _province = cityTags.get(2).selectFirst("td")
                        .select("a").text();
                if(!_province.equals(province)) continue;
                for(int i = 2; i < cityTags.size(); i++) {
                    Element cityTag = cityTags.get(i);
                    Elements cols = cityTag.select("td");
                    int dataIndex = 0;
                    if(i == 2) dataIndex++;
                    String _city = cols.get(dataIndex).selectFirst("a").text();
                    if(!_city.equals(city)) continue;
                    return cols.get(dataIndex).selectFirst("a")
                            .attr("href");
                }
            }
        }
        //没有找到查询此地区天气的URL
        return null;
    }

    /**
     * 根据IP地址查询其地理位置（省、市）
     */
    public static String[] ipLocationQuery(String ip) {
        String urlBase = "https://ip.cn/api/index?ip=%s&type=1";
        String url = String.format(urlBase, ip);
        try {
            String jsonStr = Jsoup.connect(url).ignoreContentType(true).get().text();
            JsonObject json = JsonParser.parseString(jsonStr).getAsJsonObject();
            String[] location = json.get("address").getAsString()
                    .trim().split(" ");
            String province, city;
            if(location.length >= 3) {    //国、省、市
                province = location[1];
                city = location[2];
            } else {    //国、直辖市
                province = location[1];
                city = location[1];
            }
            if(province.endsWith("省") || province.endsWith("市"))
                province = province.substring(0, province.length() - 1);
            if(city.endsWith("市"))
                city = city.substring(0, city.length() - 1);
            return new String[] { province, city };
        } catch(Exception e) {
            return null;
        }
    }

    public static final List<String> coastCitys = Arrays.asList(
            "营口", "盘锦", "锦州", "葫芦岛", "大连", "丹东", "秦皇岛", "唐山",
            "沧州", "天津", "滨州", "东营", "潍坊", "烟台", "威海", "青岛", "日照",
            "连云港", "盐城", "南通", "上海", "嘉兴", "杭州", "绍兴", "宁波", "舟山",
            "台州", "温州", "宁德", "福州", "莆田", "泉州", "厦门", "漳州", "潮州",
            "汕头", "揭阳", "汕尾", "惠州", "深圳", "中山", "珠海", "江门", "阳江",
            "茂名", "湛江", "香港", "澳门", "北海", "钦州", "防城港", "海口", "文昌",
            "琼海", "万宁", "陵水", "三亚", "乐东", "东方", "昌江", "儋州", "临高",
            "澄迈", "高雄", "台中"
    );

    public static final List<String> hillyCitys = Arrays.asList(
            "重庆", "十堰", "郴州", "怀化", "张家界", "张家口", "黄山",
            "攀枝花", "安康", "泸州", "贵阳", "遵义", "本溪", "丽水"
    );
}
