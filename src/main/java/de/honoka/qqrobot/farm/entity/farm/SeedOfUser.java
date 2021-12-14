package de.honoka.qqrobot.farm.entity.farm;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 种子背包的单个项目
 */
@Entity
@Table(name = "seed_bag")
@TableName("seed_bag")
@Data
@Accessors(chain = true)
public class SeedOfUser implements Serializable {

    @Id
    private Integer seedId;

    @Id
    private Long userQq;

    private String type;
}
