package hu.elte.computernetworks;

import hu.elte.computernetworks.model.Cluster;
import hu.elte.computernetworks.model.Node;

import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

/**
 * Created by Andras Makoviczki on 2016. 11. 27..
 */
public class RequestGenerator {

    List<Cluster> clusters;
    ScheduledExecutorService scheduledExecutorService;
    Random random;

    public RequestGenerator(List<Cluster> cluster) {
        this.clusters = cluster;
        this.random = new Random();
    }

    private void addRequest(){
        Cluster clusterSrc = clusters.get(random.nextInt(clusters.size()));
        Node nodeSrc = clusterSrc.getNodes().get(random.nextInt(clusterSrc.getSize()));
        Cluster clusterDst = clusters.get(random.nextInt(clusters.size()));
        Node nodeDst = clusterDst.getNodes().get(random.nextInt(clusterDst.getSize()));

        nodeSrc.addRequests(nodeDst.getId());
    }

    public void run(){
        scheduledExecutorService =
                Executors.newScheduledThreadPool(5);

        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                                                      public void run() {
                                                          addRequest();
                                                      }
                                                  },
                        0,random.nextInt(10),
                        TimeUnit.SECONDS);
    }

}
