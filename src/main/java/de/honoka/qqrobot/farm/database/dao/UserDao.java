package de.honoka.qqrobot.farm.database.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import de.honoka.qqrobot.farm.entity.farm.User;
import org.apache.ibatis.annotations.Param;

/**
 * (User)表数据库访问层
 *
 * @author makejava
 * @since 2021-12-09 15:28:20
 */
public interface UserDao extends BaseMapper<User> {

    void plusAssets(@Param("qq") Long qq, @Param("amount") Integer amount);

    User findAndLock(@Param("qq") long qq);

    void setTaskIdNull(User u);
}

