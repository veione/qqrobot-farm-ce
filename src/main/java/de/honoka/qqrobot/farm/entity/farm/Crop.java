package de.honoka.qqrobot.farm.entity.farm;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

/**
 * 定义一个作物实例
 */
@Entity
@Data
@Accessors(chain = true)
public class Crop implements Serializable {

    @Id
    private Integer cropId;

    @Id
    private Integer locationId;

    private String type;

    private Integer water = 50;    //水量，0~100

    private Integer growth = 1;    //当前生长季

    public static final int MAX_GROWTH = 6;        //最大生长季

    private Date lastTimeGrow = new Date();        //上一次生长的时间

    private Integer growQuality = 100;    //生长质量

    //是否已成熟
    public boolean isRiped() {
        return growth >= MAX_GROWTH;
    }

    //质量<=0时认为作物被损坏
    public boolean isBroken() {
        return growQuality <= 0;
    }

    public void reduceQuality(int amount) {
        growQuality -= amount;
    }

    public void reduceWater(int amount) {
        water -= amount;
    }

    public void increaseGrowth() {
        growth++;
    }

    public void addWater(int amount) {
        water += amount;
    }
}
