package de.honoka.qqrobot.farm.database.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import de.honoka.qqrobot.farm.entity.farm.UserTask;
import org.apache.ibatis.annotations.Param;

/**
 * (UserTask)表数据库访问层
 *
 * @author makejava
 * @since 2021-12-09 15:28:20
 */
public interface UserTaskDao extends BaseMapper<UserTask> {

    UserTask findAndLock(@Param("taskId") Integer taskId);

    Integer getLastInsertId();
}