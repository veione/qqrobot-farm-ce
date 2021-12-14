package de.honoka.qqrobot.farm.database.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import de.honoka.qqrobot.farm.entity.farm.CompensationRequest;

/**
 * (CompensationRequest)表数据库访问层
 *
 * @author makejava
 * @since 2021-12-09 15:28:18
 */
public interface CompensationRequestDao extends BaseMapper<CompensationRequest> {

    void removeOutOfDateRequest();
}

