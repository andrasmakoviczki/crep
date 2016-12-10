package hu.elte.computernetworks.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Andras Makoviczki on 2016. 11. 05..
 */
public class Cluster {
    //region fields
    private UUID id;
    private Integer capacity;
    private List<Node> nodes;
    private Integer reservedSpace;
    //endregion

    //region constructor
    public Cluster() {
    }

    private Cluster(Integer capacity, List<Node> nodes) {
        this.id = UUID.randomUUID();
        this.capacity = capacity;
        this.nodes = nodes;
        this.reservedSpace = 0;
    }

    public Cluster(Integer capacity) {
        this(capacity, new ArrayList<>());
    }
    //endregion

    //region getter setter
    public UUID getId() {
        return id;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setReservedSpace(Integer reservedSpace) {
        this.reservedSpace = reservedSpace;
    }

    @JsonIgnore
    public Integer getSize() {
        return nodes.size();
    }

    //Ha kisebbre állítjuk a cluster méretét, törli az utolsó node-okat
    public void setSize(Integer size) {
        capacity = size;
        if (size < getNodes().size()) {
            while (size < getNodes().size()) {
                removeNode();
            }
        }
    }

    @JsonIgnore
    public Integer getSpareSpace() {
        //reservedSpace + spareSpace + occupied = 2 * threshold
        return capacity - getOccupiedSpace() - reservedSpace;
    }

    @JsonIgnore
    private Integer getOccupiedSpace() {
        return nodes.size();
    }
    //endregion

    //region util
    public void addNode(Node node) {
        node.setClusterId(id);
        nodes.add(node);
    }

    public void removeNode() {
        this.nodes.remove(nodes.size() - 1);
    }

    @JsonIgnore
    public Boolean isFull() {
        return getSize() >= capacity;
    }
    //endregion
}
