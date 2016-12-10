package hu.elte.computernetworks.model;

import java.util.List;
import java.util.Random;

/**
 * Created by Andras Makoviczki on 2016. 11. 27..
 */
class RequestGenerator {
    //region fields
    private final List<Cluster> clusters;
    private final Random random;
    //endregion

    //region constructor
    RequestGenerator(List<Cluster> clusters) {
        this.clusters = clusters;
        this.random = new Random();
    }
    //endregion

    //region generate
    void generateRequests(Integer maxRequestNumber) {
        //Legalább 1 kérést generál
        Integer requestNumber = random.nextInt(maxRequestNumber) + 1;
        for (int i = 0; i < requestNumber; i++) {
            addRequest();
        }
    }

    //Egy kérés generálása
    private void addRequest() {
        Cluster clusterSrc = clusters.get(random.nextInt(clusters.size()));
        while (clusterSrc.getNodes().isEmpty()) {
            clusterSrc = clusters.get(random.nextInt(clusters.size()));
        }
        Node nodeSrc = clusterSrc.getNodes().get(random.nextInt(clusterSrc.getNodes().size()));

        Cluster clusterDst = clusters.get(random.nextInt(clusters.size()));
        while (clusterDst.getNodes().isEmpty()) {
            clusterDst = clusters.get(random.nextInt(clusters.size()));
        }
        Node nodeDst = clusterDst.getNodes().get(random.nextInt(clusterDst.getNodes().size()));

        //Saját magának nem küldhet
        while (nodeSrc.getId().equals(nodeDst.getId())) {
            clusterSrc = clusters.get(random.nextInt(clusters.size()));
            while (clusterSrc.getNodes().isEmpty()) {
                clusterSrc = clusters.get(random.nextInt(clusters.size()));
            }
            nodeSrc = clusterSrc.getNodes().get(random.nextInt(clusterSrc.getNodes().size()));

            clusterDst = clusters.get(random.nextInt(clusters.size()));
            while (clusterDst.getNodes().isEmpty()) {
                clusterDst = clusters.get(random.nextInt(clusters.size()));
            }
            nodeDst = clusterDst.getNodes().get(random.nextInt(clusterDst.getNodes().size()));
        }

        Request request;
        if (clusterSrc.getId().equals(clusterDst.getId())) {
            request = new Request(nodeSrc.getId(), nodeDst.getId());
        } else {
            request = new Request(nodeSrc.getId(), nodeDst.getId());
        }

        nodeSrc.addRequests(request);
        nodeDst.addAcceptRequests(request);
    }
    //endregion
}
