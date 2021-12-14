package de.honoka.qqrobot.farm.database.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import de.honoka.qqrobot.farm.entity.farm.Crop;
import de.honoka.qqrobot.farm.model.vo.CropVo;
import org.apache.ibatis.annotations.Param;

/**
 * (Crop)表数据库访问层
 *
 * @author makejava
 * @since 2021-12-09 15:28:18
 */
public interface CropDao extends BaseMapper<Crop> {

    Integer getCountOfLocation(@Param("locationId") Integer locationId);

    Crop findAndLock(@Param("cropId") Integer cropId,
                     @Param("locationId") Integer locationId);

    void update(Crop crop);

    void delete(Crop crop);

    CropVo findVo(@Param("cropId") Integer cropId,
                  @Param("locationId") Integer locationId,
                  @Param("lock") boolean lock);
}

