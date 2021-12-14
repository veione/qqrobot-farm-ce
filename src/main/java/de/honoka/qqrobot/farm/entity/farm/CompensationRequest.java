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
public class CompensationRequest implements Serializable {

    @Id
    @TableId(type = IdType.INPUT)
    private Integer id;

    private Long userQq;

    private Integer requestAmount;

    private Date requestTime;
}
