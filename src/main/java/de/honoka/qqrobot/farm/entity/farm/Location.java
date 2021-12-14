package de.honoka.qqrobot.farm.entity.farm;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import de.honoka.qqrobot.farm.system.SystemComponents;
import de.honoka.qqrobot.spring.boot.starter.component.ExceptionReporter;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * 地理位置，存放省、市名称，地形，当前天气，以及查询天气的链接
 */
@Entity
@Data
@Accessors(chain = true)
public class Location implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Integer locationId;

    private String province;

    private String city;

    private String landform;

    private String nowWeather;

    private String weatherUrl;

    private Long landlordQq;

    private Integer rental = 200;        //最小值为1

    private Integer landSize = 10;

    private Integer shopVolume = 10;

    /**
     * 更新天气
     */
    public void updateWeather() {
        try {
            Document doc = Jsoup.connect(weatherUrl).get();
            Element weatherTag = doc.selectFirst("#7d > ul > li")
                    .selectFirst("p.wea");
            nowWeather = weatherTag.text().split("转")[0];
        } catch(Exception e) {
            SystemComponents.applicationContext.getBean(ExceptionReporter.class)
                    .sendExceptionToDevelopingGroup(e);
        }
    }

    public String getFriendlyLocation() {
        if(province.equals(city))
            return city;
        else
            return province + city;
    }

    public int getExtendLandPrice() {
        return 10_0000 + (landSize - 10) * 20000;
    }

    public int getExtendShopPrice() {
        return 10_0000 + (shopVolume - 10) * 20000;
    }

    public void plusShopVolume() {
        shopVolume++;
    }

    public void plusLandSize() {
        landSize++;
    }
}
