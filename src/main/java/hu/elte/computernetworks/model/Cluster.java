package hu.elte.computernetworks.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andras Makoviczki on 2016. 11. 05..
 */
public class Cluster {
    private Integer id;
    private Integer capacity;
    private List<Node> nodes;

    public Cluster() {
    }

    public Cluster(Integer id, Integer capacity) {
        this(id,capacity,new ArrayList<Node>());
    }

    public Cluster(Integer id, Integer capacity, List<Node> nodes) {
        this.id = id;
        this.capacity = capacity;
        this.nodes = nodes;
    }

    public void add(Node node){
        node.setClusterid(id);
        nodes.add(node);
    }

    @JsonIgnore
    public Integer getSize(){
        return nodes.size();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
}
