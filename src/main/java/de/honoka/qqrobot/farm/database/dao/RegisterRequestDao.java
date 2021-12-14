package de.honoka.qqrobot.farm.database.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import de.honoka.qqrobot.farm.entity.farm.RegisterRequest;
import org.apache.ibatis.annotations.Param;

/**
 * (RegisterRequest)表数据库访问层
 *
 * @author makejava
 * @since 2021-12-09 15:28:20
 */
public interface RegisterRequestDao extends BaseMapper<RegisterRequest> {

    RegisterRequest findAndLock(String id);

    RegisterRequest getAvaliableRequest(@Param("qq") long qq);
}

