package de.honoka.qqrobot.farm.database.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import de.honoka.qqrobot.farm.entity.system.UsageLog;

/**
 * (UsageLog)表数据库访问层
 *
 * @author makejava
 * @since 2021-12-09 15:28:20
 */
public interface UsageLogDao extends BaseMapper<UsageLog> {

    int getCount();
}

