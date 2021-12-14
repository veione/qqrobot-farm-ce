package de.honoka.qqrobot.farm.database.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import de.honoka.qqrobot.farm.entity.farm.SeedOfUser;
import org.apache.ibatis.annotations.Param;

/**
 * (SeedBag)表数据库访问层
 *
 * @author makejava
 * @since 2021-12-09 15:28:20
 */
public interface SeedOfUserDao extends BaseMapper<SeedOfUser> {

    SeedOfUser findAndLock(@Param("seedId") int seedId, @Param("qq") long qq);

    Integer getCountOfUser(@Param("qq") long qq);

    void update(SeedOfUser seedOfUser);

    void delete(SeedOfUser seedOfUser);
}

