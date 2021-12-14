package de.honoka.qqrobot.farm.entity.farm;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * 定义作物类型标准
 */
@Entity
@Data
@Accessors(chain = true)
public class CropType implements Serializable {

    @Id
    @TableId(type = IdType.INPUT)
    private String name;

    private String season;

    private String landform;

    private Integer seedPrice;

    private Integer fruitPrice;
}
