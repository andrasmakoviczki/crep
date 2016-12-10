package hu.elte.computernetworks.model;

import java.util.UUID;

/**
 * Created by Andras Makoviczki on 2016. 11. 05..
 */
public class Request {
    //region fields
    private final UUID src;
    private final UUID dst;
    //endregion

    //region constructor
    public Request(UUID src, UUID dst) {
        this.src = src;
        this.dst = dst;
    }
    //endregion

    //region getter setter
    public UUID getSrc() {
        return src;
    }

    public UUID getDst() {
        return dst;
    }
    //endregion
}
