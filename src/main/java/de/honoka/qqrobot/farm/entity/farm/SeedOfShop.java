package de.honoka.qqrobot.farm.entity.farm;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 种子商店的单个项目
 */
@Entity
@Table(name = "seed_shop")
@TableName("seed_shop")
@Data
@Accessors(chain = true)
public class SeedOfShop implements Serializable {

    @Id
    private Integer seedId;

    @Id
    private Integer locationId;

    private String type;

    private Integer price;
}
