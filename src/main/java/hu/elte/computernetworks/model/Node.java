package hu.elte.computernetworks.model;

import hu.elte.computernetworks.model.Request;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andras Makoviczki on 2016. 11. 05..
 */
public class Node {
    private Integer id;
    private Integer clusterid;
    private List<Request> requests;

    public Node() {
    }

    public Node(Integer id) {
        this(id,0);
    }

    public Node(Integer id, Integer clusterid) {
        this.id = id;
        this.clusterid = clusterid;
        this.requests = new ArrayList<Request>();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getClusterid() {
        return clusterid;
    }

    public void setClusterid(Integer clusterid) {
        this.clusterid = clusterid;
    }

    public List<Request> getRequests() {
        return requests;
    }

    public void setRequests(List<Request> requests) {
        this.requests = requests;
    }

    public void addRequests(Integer dst) {
        requests.add(new Request(5,id,dst));
    }
}
