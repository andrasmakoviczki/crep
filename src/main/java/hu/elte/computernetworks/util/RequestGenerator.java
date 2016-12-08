package hu.elte.computernetworks.util;

import hu.elte.computernetworks.model.Cluster;
import hu.elte.computernetworks.model.Node;
import hu.elte.computernetworks.model.Request;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Andras Makoviczki on 2016. 11. 27..
 */
public class RequestGenerator {
    //region fields
    List<Cluster> clusters;
    Random random;
    //endregion

    //region constructor
    public RequestGenerator(List<Cluster> clusters) {
        this.clusters = clusters;
        this.random = new Random();
    }
    //endregion

    private void addRequest() {
        Cluster clusterSrc = clusters.get(random.nextInt(clusters.size()));
        while (clusterSrc.getNodes().isEmpty()){
            clusterSrc = clusters.get(random.nextInt(clusters.size()));
        }
        Node nodeSrc = clusterSrc.getNodes().get(random.nextInt(clusterSrc.getNodes().size()));

        Cluster clusterDst = clusters.get(random.nextInt(clusters.size()));
        while (clusterDst.getNodes().isEmpty()){
            clusterDst = clusters.get(random.nextInt(clusters.size()));
        }
        Node nodeDst = clusterDst.getNodes().get(random.nextInt(clusterDst.getNodes().size()));

        //Saját magának nem küldhet
        while (nodeSrc.getId().equals(nodeDst.getId())) {
            clusterSrc = clusters.get(random.nextInt(clusters.size()));
            while (clusterSrc.getNodes().isEmpty()){
                clusterSrc = clusters.get(random.nextInt(clusters.size()));
            }
            nodeSrc = clusterSrc.getNodes().get(random.nextInt(clusterSrc.getNodes().size()));

            clusterDst = clusters.get(random.nextInt(clusters.size()));
            while (clusterDst.getNodes().isEmpty()){
                clusterDst = clusters.get(random.nextInt(clusters.size()));
            }
            nodeDst = clusterDst.getNodes().get(random.nextInt(clusterDst.getNodes().size()));
        }

        Request request;
        //Kérés költség
        if (clusterSrc.getId().equals(clusterDst.getId())) {
            request = new Request(nodeSrc.getId(),nodeDst.getId(),0);
        } else {
            request = new Request(nodeSrc.getId(),nodeDst.getId(),1);
        }

        nodeSrc.addRequests(request);
        nodeDst.acceptRequests(request);
    }

    public void generateRequests(Integer maxRequestNumber) {
        //Legalább 1 kérést generál
        Integer requestNumber = random.nextInt(maxRequestNumber) + 1;
        for (int i = 0; i < requestNumber; i++) {
            addRequest();
        }
    }
}
