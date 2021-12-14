package de.honoka.qqrobot.farm.database.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import de.honoka.qqrobot.farm.entity.farm.SeedOfShop;
import org.apache.ibatis.annotations.Param;

/**
 * (SeedShop)表数据库访问层
 *
 * @author makejava
 * @since 2021-12-09 15:28:20
 */
public interface SeedOfShopDao extends BaseMapper<SeedOfShop> {

    void deleteAllSeedOfShop(@Param("locationId") Integer locationId);

    SeedOfShop findAndLock(@Param("seedId") int seedId,
                           @Param("locationId") Integer locationId);

    void update(SeedOfShop seedOfShop);

    void delete(SeedOfShop seedOfShop);
}