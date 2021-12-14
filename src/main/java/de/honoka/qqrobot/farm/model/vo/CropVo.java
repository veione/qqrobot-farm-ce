package de.honoka.qqrobot.farm.model.vo;

import de.honoka.qqrobot.farm.entity.farm.Crop;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@Data
@Accessors(chain = true)
public class CropVo {

    private Integer cropId;

    private Integer locationId;

    private String type;

    private Integer water;

    private Integer growth;

    private Date lastTimeGrow;

    private Integer growQuality;

    //vo fields

    private String locationNowWeather;

    private String locationLandform;

    private String typeSeason;

    private String typeLandform;

    public Crop toCrop() {
        Crop crop = new Crop();
        BeanUtils.copyProperties(this, crop);
        return crop;
    }
}
