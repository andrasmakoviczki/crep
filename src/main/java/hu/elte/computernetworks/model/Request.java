package hu.elte.computernetworks.model;

/**
 * Created by Andras Makoviczki on 2016. 11. 05..
 */
public class Request {
    private Integer id;
    private Integer src;
    private Integer dst;
    private Integer length;
    private Integer cost;

    public Request(Integer id, Integer src, Integer dst){
        this(id,src,dst,1,1);
    }

    public Request(Integer id, Integer src, Integer dst,Integer cost){
        this(id,src,dst,1,cost);
    }

    public Request(Integer id, Integer src, Integer dst, Integer length, Integer cost) {
        this.id = id;
        this.src = src;
        this.dst = dst;
        this.length = length;
        this.cost = cost;
    }
}
