package de.honoka.qqrobot.farm.database.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import de.honoka.qqrobot.farm.entity.system.ExceptionRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (ExceptionRecord)表数据库访问层
 *
 * @author makejava
 * @since 2021-12-09 15:28:18
 */
public interface ExceptionRecordDao extends BaseMapper<ExceptionRecord> {

    List<ExceptionRecord> readException(@Param("limit") int exceptionRecordMaxSize);
}