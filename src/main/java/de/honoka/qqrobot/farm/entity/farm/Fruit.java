package de.honoka.qqrobot.farm.entity.farm;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 果实背包的单个项目
 */
@Entity
@Table(name = "fruit_bag")
@TableName("fruit_bag")
@Data
@Accessors(chain = true)
public class Fruit implements Serializable {

    @Id
    private Integer fruitId;

    @Id
    private Long userQq;

    private String type;
}
