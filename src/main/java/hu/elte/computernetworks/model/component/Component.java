package hu.elte.computernetworks.model.component;

import hu.elte.computernetworks.model.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Andras Makoviczki on 2016. 11. 28..
 */
public class Component {
    //region fields
    private final UUID id;
    private final List<Node> nodes;
    private Integer reservedSpace;
    //endregion

    //region constructor
    public Component() {
        this(new ArrayList<>());
    }

    public Component(List<Node> nodes) {
        this.id = UUID.randomUUID();
        this.nodes = nodes;
        this.reservedSpace = 1;
    }
    //endregion

    //region getter setter
    public UUID getId() {
        return id;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public Integer getReservedSpace() {
        return reservedSpace;
    }

    public void setReservedSpace(Integer reservedSpace) {
        this.reservedSpace = reservedSpace;
    }

    public Integer getSize() {
        return nodes.size();
    }
    //endregion

    //region util
    public Boolean isSingleton() {
        return (getSize() == 1);
    }
    //endregion
}
