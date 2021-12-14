package de.honoka.qqrobot.farm.database.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import de.honoka.qqrobot.farm.entity.farm.Location;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (Location)表数据库访问层
 *
 * @author makejava
 * @since 2021-12-09 15:28:20
 */
public interface LocationDao extends BaseMapper<Location> {

    Location findAndLock(@Param("locationId") Integer locationId);

    List<Integer> findAllId();

    Location findByProvinceAndCity(@Param("province") String province,
                                   @Param("city") String city);
}

