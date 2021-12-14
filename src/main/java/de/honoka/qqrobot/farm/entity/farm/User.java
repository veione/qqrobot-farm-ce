package de.honoka.qqrobot.farm.entity.farm;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

@Entity
@Data
@Accessors(chain = true)
public class User implements Serializable {

    @Id
    @TableId(type = IdType.INPUT)
    private Long qq;

    private Integer locationId;

    private Integer taskId;

    private Integer assets = 5000;        //资产

    private Integer level = 1;        //用户等级

    /* 这个成员的初始化使用了level变量，若level变量在它之后定义，
       则level的初始值无法被使用，而默认为0 */
    private Integer waterRemaining = getMaxWaterRemaining();    //余水量

    private Date lastTimeSowSeed = new Date();

    //最大余水量
    public int getMaxWaterRemaining() {
        return 200 + (level - 1) * 100;
    }

    public int getUpdateLevelPrice() {
        return 20000 + (level - 1) * 10000;
    }

    public int getFruitBagVolume() {
        return 10 + (level - 1);
    }

    public int getSeedBagVolume() {
        return 10 + (level - 1);
    }

    public void plusAssets(int amount) {
        assets += amount;
    }

    public void reduceAssets(int amount) {
        assets -= amount;
    }
}
