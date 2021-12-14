package de.honoka.qqrobot.farm.database.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import de.honoka.qqrobot.farm.entity.farm.ChangeLocationRequest;
import org.apache.ibatis.annotations.Param;

/**
 * (ChangeLocationRequest)表数据库访问层
 *
 * @author makejava
 * @since 2021-12-09 15:28:16
 */
public interface ChangeLocationRequestDao extends BaseMapper<ChangeLocationRequest> {

    ChangeLocationRequest findAndLock(@Param("id") String id);

    ChangeLocationRequest getAvaliableRequest(@Param("qq") long qq);

    ChangeLocationRequest getLatestSuccessRequest(@Param("qq") long qq);
}

