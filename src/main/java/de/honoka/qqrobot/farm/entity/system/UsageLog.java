package de.honoka.qqrobot.farm.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Data
@Accessors(chain = true)
public class UsageLog implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Integer id;

    //时间戳
    private Date datetime;

    //QQ号码
    private Long qq;

    //群名，群名片或昵称，执行的操作，处理的信息，回复的信息
    private String groupName;

    private String username;

    private String msg;

    @Column(columnDefinition = "text")
    private String reply;
}
