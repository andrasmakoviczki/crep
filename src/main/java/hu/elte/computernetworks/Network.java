package hu.elte.computernetworks;

import hu.elte.computernetworks.model.Cluster;
import hu.elte.computernetworks.model.Node;
import hu.elte.computernetworks.util.Load;
import hu.elte.computernetworks.util.Save;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

/**
 * Created by Andras Makoviczki on 2016. 11. 27..
 */
public class Network {

    private static final Logger log = LoggerFactory.getLogger(Network.class);

    private Integer clusterSize;

    private List<Cluster> clusters;

    public Network() {
        this(new ArrayList<Cluster>(),4);
    }

    public Network(List<Cluster> clusters) {
        this(clusters,4);
    }

    public Network(List<Cluster> clusters,Integer clusterSize) {
        this.clusters = clusters;
        this.clusterSize = clusterSize;
    }

    public void addCluster(){
        clusters.add(new Cluster(10,clusterSize));
    }

    public void testRun(){
        RequestGenerator r = new RequestGenerator(clusters);
        r.run();
    }

    public void testLoad(){
        Load l = new Load();
        Network network;
        try {
            network = l.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void testSave(){
        this.clusterSize = 4;
        this.clusters = new ArrayList<Cluster>();

        Cluster c1 = new Cluster(1,clusterSize);
        Cluster c2 = new Cluster(2,clusterSize);
        Cluster c3 = new Cluster(2,clusterSize);

        Node n1 = new Node(1);
        Node n2 = new Node(2);
        Node n3 = new Node(3);
        Node n7 = new Node(3);


        c1.add(n1);
        c1.add(n2);
        c2.add(n3);
        c3.add(n7);


        clusters.add(c1);
        clusters.add(c2);
        clusters.add(c3);

        Network network = new Network(clusters,clusterSize);

        Save s = new Save();

        try {
            s.write(network);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Integer getClusterSize() {
        return clusterSize;
    }

    public void setClusterSize(Integer clusterSize) {
        this.clusterSize = clusterSize;
    }

    public List<Cluster> getClusters() {
        return clusters;
    }

    public void setClusters(List<Cluster> clusters) {
        this.clusters = clusters;
    }
}
