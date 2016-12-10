package hu.elte.computernetworks.model.component;

import java.util.UUID;

/**
 * Created by Andras Makoviczki on 2016. 12. 01..
 */
public class ComponentEdge {
    //region fields
    private final UUID srcNodeId;
    private final UUID dstNodeId;
    private UUID srcComponentId;
    private UUID dstComponentId;
    private Integer weight;
    //endregion

    //region constructor
    public ComponentEdge(UUID srcComponentId, UUID dstComponentId, UUID srcNodeId, UUID dstNodeId, Integer weight) {
        this.srcComponentId = srcComponentId;
        this.dstComponentId = dstComponentId;
        this.srcNodeId = srcNodeId;
        this.dstNodeId = dstNodeId;
        this.weight = weight;
    }
    //endregion

    //region getter setter
    public UUID getSrcComponentId() {
        return srcComponentId;
    }

    public void setSrcComponentId(UUID srcComponentId) {
        this.srcComponentId = srcComponentId;
    }

    public UUID getDstComponentId() {
        return dstComponentId;
    }

    public void setDstComponentId(UUID dstComponentId) {
        this.dstComponentId = dstComponentId;
    }

    public UUID getSrcNodeId() {
        return srcNodeId;
    }

    public UUID getDstNodeId() {
        return dstNodeId;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }
    //endregion
}
