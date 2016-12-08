package hu.elte.computernetworks.model;

import java.util.UUID;

/**
 * Created by Andras Makoviczki on 2016. 11. 05..
 */
public class Request {
    //region fields
    private UUID id;
    private UUID src;
    private UUID dst;
    private Integer cost;
    //endregion

    //region constructor
    public Request(UUID src, UUID dst, Integer cost) {
        this.id = UUID.randomUUID();
        this.src = src;
        this.dst = dst;
        this.cost = cost;
    }
    //endregion

    //region getter setter
    public UUID getSrc() {
        return src;
    }

    public UUID getDst() {
        return dst;
    }

    public Integer getCost() {
        return cost;
    }

    public void setCost(Integer cost) {
        this.cost = cost;
    }
    //endregion
}
