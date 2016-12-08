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
    private Integer threshold;
    //endregion

    //region constructor
    public Cluster() {
    }

    public Cluster(Integer capacity, List<Node> nodes) {
        this.id = UUID.randomUUID();
        this.capacity = capacity;
        this.nodes = nodes;
        this.threshold = capacity / 2;
        this.reservedSpace = 1;
    }

    public Cluster(Integer capacity) {
        this(capacity,new ArrayList<Node>());
    }

    //endregion

    //region getter setter
    @JsonIgnore
    public Integer getSize(){
        return nodes.size();
    }

    public UUID getId() {
        return id;
    }

    @JsonIgnore
    public Boolean isFull(){
        return getSize() >= capacity ? true : false;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public Integer getReservedSpace() {
        return reservedSpace;
    }

    public void setReservedSpace(Integer reservedSpace) {
        this.reservedSpace = reservedSpace;
    }

    public Integer getThreshold() {
        return threshold;
    }

    public void setThreshold(Integer threshold) {
        this.threshold = threshold;
    }

    //reservedSpace + spareSpace + occupied = 2 * threshold
    @JsonIgnore
    public Integer getSpareSpace() {
        return capacity - getOccupiedSpace() - reservedSpace;
    }
    @JsonIgnore
    public Integer getOccupiedSpace(){
        return nodes.size();
    }

    //endregion

    public void addNode(Node node){
        node.setClusterId(id);
        nodes.add(node);
    }

    public void removeNode() {
        this.nodes.remove(nodes.size()-1);
    }

    //Ha kisebbre állítjuk a cluster méretét, törli az utolsó node-okat
    public void setSize(Integer size){
        capacity = size;
        if(size < getNodes().size()){
            while (size < getNodes().size()){
                removeNode();
            }
        }
    }
}
