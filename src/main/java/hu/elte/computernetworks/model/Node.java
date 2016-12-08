package hu.elte.computernetworks.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Andras Makoviczki on 2016. 11. 05..
 */
public class Node {
    //region fields
    private UUID id;
    private UUID clusterId;
    private List<Request> requests;
    private List<Request> acceptedRequests;
    //endregion

    //region constructor
    public Node() {
    }

    public Node(UUID clusterId) {
        this.id = UUID.randomUUID();
        this.clusterId = clusterId;
        this.requests = new ArrayList<>();
        this.acceptedRequests = new ArrayList<>();
    }
    //endregion

    //region getter setter
    public UUID getId() {
        return id;
    }

    public UUID getClusterId() {
        return clusterId;
    }

    public void setClusterId(UUID clusterId) {
        this.clusterId = clusterId;
    }

    public List<Request> getRequests() {
        return requests;
    }

    public void setRequests(List<Request> requests) {
        this.requests = requests;
    }

    public List<Request> getAcceptedRequests() {
        return acceptedRequests;
    }

    public void addRequests(UUID dst, Integer cost) {
        requests.add(new Request(id, dst,cost));
    }

    public void addRequests(Request request) {
        requests.add(request);
    }

    //Fogadott requests-ek nyílvántartása
    public void acceptRequests(UUID dst,Integer cost){
        acceptedRequests.add(new Request(dst,id,cost));
    }

    public void acceptRequests(Request request){
        acceptedRequests.add(request);
    }

    public void setAcceptedRequests(List<Request> acceptedRequests) {
        this.acceptedRequests = acceptedRequests;
    }

    //endregion
}
