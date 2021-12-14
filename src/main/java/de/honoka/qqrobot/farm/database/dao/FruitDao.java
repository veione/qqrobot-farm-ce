package de.honoka.qqrobot.farm.database.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import de.honoka.qqrobot.farm.entity.farm.Fruit;
import org.apache.ibatis.annotations.Param;

/**
 * (FruitBag)表数据库访问层
 *
 * @author makejava
 * @since 2021-12-09 15:28:19
 */
public interface FruitDao extends BaseMapper<Fruit> {

    Integer getCountOfUser(@Param("qq") Long qq);

    Fruit findAndLock(@Param("fruitId") int fruitId, @Param("qq") long qq);

    void update(Fruit fruit);

    void delete(Fruit fruit);
}

