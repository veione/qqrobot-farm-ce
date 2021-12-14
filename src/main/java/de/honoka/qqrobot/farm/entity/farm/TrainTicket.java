package de.honoka.qqrobot.farm.entity.farm;

import de.honoka.qqrobot.farm.util.train.ticket.TrainTicketUtils;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

@Entity
@Data
@Accessors(chain = true)
public class TrainTicket implements Serializable {

    @Id
    private Integer ticketId;

    @Id
    private Long userQq;

    private String trainNo;

    private String fromStation;

    private String toStation;

    private Integer fromLocation;

    private Integer toLocation;

    private Date startTime;

    private Date arriveTime;

    private Integer price;

    private Boolean used = false;

    @Override
    public String toString() {
        String str = "车次：" + trainNo;
        str += String.format("\n始到站：%s → %s", fromStation, toStation);
        str += "\n开车时间：" + TrainTicketUtils.getDisplayTimeStr(startTime);
        str += "\n到达时间：" + TrainTicketUtils.getDisplayTimeStr(arriveTime);
        str += "\n票价：" + price;
        return str;
    }
}
