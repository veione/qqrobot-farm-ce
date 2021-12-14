package de.honoka.qqrobot.farm.database.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import de.honoka.qqrobot.farm.entity.farm.TrainTicket;
import org.apache.ibatis.annotations.Param;

/**
 * (TrainTicket)表数据库访问层
 *
 * @author makejava
 * @since 2021-12-09 15:28:20
 */
public interface TrainTicketDao extends BaseMapper<TrainTicket> {

    void update(TrainTicket trainTicket);

    void delete(TrainTicket trainTicket);

    void removeOutOfDateTickets();

    TrainTicket findAndLock(@Param("ticketId") int ticketId,
                            @Param("qq") long qq);
}

