package hu.elte.computernetworks.model.component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Andras Makoviczki on 2016. 12. 01..
 */
public class ComponentEdge {
    //region fields
    private UUID srcComponentId;
    private UUID dstComponentId;
    private UUID srcNodeId;
    private UUID dstNodeId;
    private Integer weight;
    //endregion

    //region constructor
    public ComponentEdge(){
    }

    public ComponentEdge(UUID srcComponentId, UUID dstComponentId, UUID srcNodeId, UUID dstNodeId, Integer weight) {
        this.srcComponentId = srcComponentId;
        this.dstComponentId = dstComponentId;
        this.srcNodeId = srcNodeId;//new HashSet<>();
        //this.srcNodeId.add(srcComponentId);
        this.dstNodeId = dstNodeId;
        this.weight = weight;
    }

    //endregion

    //region getter setter
    public UUID getSrcComponentId() {
        return srcComponentId;
    }

    public UUID getDstComponentId() {
        return dstComponentId;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public void setSrcComponentId(UUID srcComponentId) {
        this.srcComponentId = srcComponentId;
    }

    public void setDstComponentId(UUID dstComponentId) {
        this.dstComponentId = dstComponentId;
    }

    public UUID getSrcNodeId() {
        return srcNodeId;
    }

    public void setSrcNodeId(UUID srcNodeId) {
        this.srcNodeId = srcNodeId;
    }

    public UUID getDstNodeId() {
        return dstNodeId;
    }

    public void setDstNodeId(UUID dstNodeId) {
        this.dstNodeId = dstNodeId;
    }

    //endregion
}
