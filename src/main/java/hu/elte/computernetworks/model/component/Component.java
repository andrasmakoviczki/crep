package hu.elte.computernetworks.model.component;

import hu.elte.computernetworks.model.Node;

import java.util.*;

/**
 * Created by Andras Makoviczki on 2016. 11. 28..
 */
public class Component {
    //region fields
    private UUID id;
    private List<Node> nodes;
    private Integer reservedSpace;
    //endregion

    //region constructor
    public Component(){
        this(new ArrayList<>());
    }

    public Component(List<Node> nodes) {
        this(nodes,1);
    }

    public Component(List<Node> nodes, Integer reservedSpace) {
        this.id = UUID.randomUUID();
        this.nodes = nodes;
        this.reservedSpace = reservedSpace;
    }

    //endregion

    //region getter setter
    public UUID getId() {
        return id;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public Integer getSize(){
        return nodes.size();
    }

    public Integer getReservedSpace() {
        return reservedSpace;
    }

    public void setReservedSpace(Integer reservedSpace) {
        this.reservedSpace = reservedSpace;
    }

    public Boolean isSingleton(){
        return (getSize() == 1) ? true : false;
    }
    //endregion
}
