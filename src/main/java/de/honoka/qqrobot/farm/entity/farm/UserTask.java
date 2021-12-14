package de.honoka.qqrobot.farm.entity.farm;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Data
@Accessors(chain = true)
public class UserTask implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Integer taskId;

    private Long userQq;

    private String type;

    private Date startTime;

    private Date finishTime;

    //与任务相关联的参数，Json格式文本
    @Column(columnDefinition = "text")
    private String referencedParams;

    public void setReferencedParamsJson(JsonObject json) {
        referencedParams = json.toString();
    }

    public JsonObject getReferencedParamsJson() {
        return JsonParser.parseString(referencedParams).getAsJsonObject();
    }
}
