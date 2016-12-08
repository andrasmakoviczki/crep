package hu.elte.computernetworks.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import hu.elte.computernetworks.model.component.Component;
import hu.elte.computernetworks.model.component.ComponentEdge;
import hu.elte.computernetworks.model.component.ComponentGraph;
import hu.elte.computernetworks.model.component.ComponentSet;
import hu.elte.computernetworks.util.Load;
import hu.elte.computernetworks.util.RequestGenerator;
import hu.elte.computernetworks.util.Save;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Created by Andras Makoviczki on 2016. 11. 27..
 */
public class Network {

    private static final Logger log = LoggerFactory.getLogger(Network.class);

    //region fields
    private Integer DEFAULT_CLUSTER_SIZE = 4;
    private Integer DEFAULT_THRESHOLD = 4;
    private Integer DEFAULT_MIGRATION_COST= 3;
    private Integer threshold;
    private Integer migrationCost;
    private Integer clusterSize;
    private List<Cluster> clusters;
    private ComponentGraph graph;
    //endregion

    //region constructor
    public Network() {
        this(new ArrayList<Cluster>());
    }

    public Network(List<Cluster> clusters) {
        this.clusters = clusters;
        this.clusterSize = DEFAULT_CLUSTER_SIZE;
        this.threshold = DEFAULT_THRESHOLD;
        this.migrationCost = DEFAULT_MIGRATION_COST;
    }
    //endregion

    //region test
    public void testRequest() {
        CREP();
    }

    public void testStress() {
        Integer clusterNumber = 2000;
        Integer nodeNumber = 100;
        Integer clusterSize = 100;
        List<Cluster> clusters = new ArrayList<Cluster>();

        Random random = new Random();

        for (int i = 0; i < clusterNumber; i++) {
            Cluster c = new Cluster(clusterSize);
            List<Node> n = new ArrayList<>();
            for (int j = 0; j < random.nextInt(nodeNumber); j++) {
                n.add(new Node(c.getId()));
                c.setNodes(n);
            }
            clusters.add(c);
        }

        Save s = new Save();

        try {
            s.write(new Network(clusters));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void testLoad() {
        Load l = new Load();
        Network network;
        try {
            network = l.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void testSave() {
        this.clusterSize = 4;
        this.clusters = new ArrayList<Cluster>();

        Cluster c1 = new Cluster(clusterSize);
        Cluster c2 = new Cluster(clusterSize);
        Cluster c3 = new Cluster(clusterSize);

        Node n1 = new Node(UUID.randomUUID());
        Node n2 = new Node(UUID.randomUUID());
        Node n3 = new Node(UUID.randomUUID());
        Node n7 = new Node(UUID.randomUUID());

        c1.addNode(n1);
        c1.addNode(n2);
        c2.addNode(n3);
        c3.addNode(n7);

        clusters.add(c1);
        clusters.add(c2);
        clusters.add(c3);

        Network network = new Network(clusters);

        Save s = new Save();

        try {
            s.write(network);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //endregion

    //region getter setter
    public Integer getClusterSize() {
        return clusterSize;
    }

    public void setClusterSize(Integer clusterSize) {
        this.clusterSize = clusterSize;
        clusters.forEach(cluster -> cluster.setSize(clusterSize));
    }

    public List<Cluster> getClusters() {
        return clusters;
    }

    public void setClusters(List<Cluster> clusters) {
        this.clusters = clusters;
    }
    //endregion

    //region implemented method


    public Integer getMigrationCost() {
        return migrationCost;
    }

    public void setMigrationCost(Integer migrationCost) {
        this.migrationCost = migrationCost;
    }

    public void addCluster() {
        clusters.add(new Cluster(clusterSize));
    }

    public void addNode(Integer cluster) throws Exception {
        if (clusters.get(cluster).getNodes().size() >= clusterSize) {
            throw new Exception();
        }
        clusters.get(cluster).addNode(new Node(clusters.get(cluster).getId()));
    }

    public void removeNode(Integer cluster) throws Exception {
        if (clusters.get(cluster).getNodes().size() <= 0) {
            throw new Exception();
        }
        clusters.get(cluster).removeNode();
    }

    public void removeCluster(Integer cluster) throws Exception {
        if (clusters.size() <= 0) {
            throw new Exception();
        }
        clusters.remove((int) cluster);
    }

    public List<Node> gatherNodes() {
        List<Node> nodes = new ArrayList<Node>();
        clusters.stream().forEach(cluster ->
                cluster.getNodes().stream()
                        .filter(node -> node.getRequests().size() > 0 /*|| node.getAcceptedRequests().size() > 0*/)
                        .forEach(node -> nodes.add(node))
        );
        return nodes;
    }

    public List<ComponentEdge> setComponentEdge(List<Component> componentList){
    List<ComponentEdge> edges = new ArrayList<ComponentEdge>();
        graph.getComponentList().stream().forEach(component -> component.getNodes().stream()
                .forEach(node -> node.getRequests().stream()
                .forEach(request -> {
                    //Új komponens létrehozása
                    List<Node> nList = new ArrayList<Node>();
                    Node dstNode = findNodeById(request.getDst());
                    nList.add(dstNode);
                    Component dstComponent = null;
                    //ha a dstNode nincs benne
                    if(graph.findComponentByNodeId(dstNode.getId()) == null){
                        dstComponent = new Component(nList);
                        graph.addComponent(dstComponent);
                    } else {
                        dstComponent = graph.findComponentByNodeId(dstNode.getId());
                    }
                    //Új él létrehozása
                    edges.add(new ComponentEdge(component.getId(),dstComponent.getId(),node.getId(),dstNode.getId(),1));
                }))
        );
        return edges;
    }

    public void setGraph(){
        List<Request> requestList = getRequests();
        Component srcComponent = null;
        Component dstComponent = null;
        Node srcNode = null;
        Node dstNode = null;

        for (Request request: requestList) {
            srcNode = findNodeById(request.getSrc());
            dstNode = findNodeById(request.getDst());
            if(srcNode == null || dstNode == null){
                throw new UnsupportedOperationException();
            }

            if(graph.findComponentByNodeId(request.getSrc()) == null){
                List<Node> nodeList = new ArrayList<>();
                nodeList.add(srcNode);
                srcComponent = new Component(nodeList);
                graph.addComponent(srcComponent);
            } else {
                srcComponent = graph.findComponentByNodeId(request.getSrc());
            }

            if(graph.findComponentByNodeId(request.getDst()) == null){
                List<Node> nodeList = new ArrayList<>();
                nodeList.add(dstNode);
                dstComponent = new Component(nodeList);
                graph.addComponent(dstComponent);
            } else {
                dstComponent = graph.findComponentByNodeId(request.getDst());
            }

            graph.addEdge(new ComponentEdge(srcComponent.getId(),dstComponent.getId(),srcNode.getId(),dstNode.getId(),1));
        }
    }

    public Node findNodeById(UUID nodeId){
        Node node = null;
        for (Cluster c: clusters) {
            for (Node n: c.getNodes()) {
                if(n.getId().equals(nodeId)){
                    node = n;
                    break;
                }
            }
        }
        return node;
    }

    @JsonIgnore
    public Integer getNodeSize() {
        return clusters.stream().mapToInt(cluster -> cluster.getSize()).sum();
    }

    public void ConstructGraph() {
        graph = new ComponentGraph(gatherNodes());
        setGraph();
    }

    public void generateRequests() {
        resetRequests();
        RequestGenerator r = new RequestGenerator(clusters);
        r.generateRequests(getNodeSize());
    }

    @JsonIgnore
    public ComponentGraph getGraph() {
        return graph;
    }

    public void resetRequests() {
        clusters.stream().forEach(cluster -> cluster.getNodes().stream().forEach(node -> {
            node.setRequests(new ArrayList<Request>());
            node.setAcceptedRequests(new ArrayList<Request>());
        }));
        graph = null;
    }

    public void CREP() {
        //generateRequests();
        ConstructGraph();
        List<Integer> sList = testSpare();
        for (Request request : getRequests()) {
            //Keep track of communication cost
            ComponentEdge cEdge = graph.findEdge(request);
            increaseCommunicatinCost(cEdge);
            //Merge components
            merge();
            //End of a Y-epoch
            split();
        }
    }

    public void increaseCommunicatinCost(ComponentEdge cEdge) {
        //TODO
        Component src = graph.getComponentByComponentId(cEdge.getSrcComponentId());
        Component dst = graph.getComponentByComponentId(cEdge.getDstComponentId());

        if (!src.getId().equals(dst.getId())) {
            cEdge.setWeight(cEdge.getWeight() + 1);
        }

        if (src == null || dst == null){
            System.out.println("");
        }
    }

    public void merge() {
        ComponentSet X = graph.getX();
        if (X.getComponentSize() > 1) {
            Integer _xSize = X.getComponentSize();
            //Komponens halmaz uniója új komponensé
            Component newComponent = new Component();
            X.getComponentSet().stream().forEach(component -> component.getNodes().stream().
                    forEach(node -> newComponent.getNodes().add(node)));

            //Diszjunk komponens halmaz
            ComponentSet disjunctComponentSet = new ComponentSet(new HashSet<>(graph.getComponentList()));
            //TODO: check nem üres
            disjunctComponentSet.getComponentSet().removeAll(X.getComponentSet());

            //Azok az élek, ahol a src X-beli és dst nem X-beli
            Set<ComponentEdge> edges = graph.getEdges().stream()
                    .filter(componentEdge -> X.getComponentSet().contains(componentEdge.getSrcComponentId())
                            && disjunctComponentSet.getComponentSet().contains(componentEdge.getDstComponentId()))
                    .collect(Collectors.toSet());

            //Csoportosítjuk végpont szerint és összegezzük az éleket
            Map<UUID, Integer> edgeDstWeightPair = edges.stream().collect(Collectors.groupingBy(ComponentEdge::getDstComponentId,
                    Collectors.summingInt(ComponentEdge::getWeight)));

            //Új élek létrehozása az új komponens alapján
            edgeDstWeightPair.entrySet().stream()
                    .forEach(uuidIntegerEntry ->
                            graph.addEdge(
                                    new ComponentEdge(
                                            newComponent.getId(), uuidIntegerEntry.getKey(),
                                            //TODO: check
                                            null,graph.findComponentByNodeId(uuidIntegerEntry.getKey()).getNodes().get(0).getId()
                                            ,uuidIntegerEntry.getValue())));

            //Csökkenő sorrendbe rendezi a komponenseket reservedSpace szerint
            Comparator<Component> componentComparator = (o1,o2)->Integer.compare(o1.getReservedSpace(),o2.getReservedSpace());
            Iterator<Component> componentIterator = X.getComponentSet().stream()
                    .sorted(componentComparator.reversed())
                    .iterator();

            Component largestSpace = null;
            Cluster c = null;

            while(componentIterator.hasNext()){
                largestSpace = X.getLargestReservedSpace();
                for (Node n:largestSpace.getNodes()) {
                    c = findCluster(n.getClusterId());
                    if(!c.isFull()){
                        break;
                    } else {
                        c = null;
                    }
                }
                if(c != null){
                    break;
                } else {
                    largestSpace = null;
                }
            }

            if(largestSpace == null){
                throw new UnsupportedOperationException();
            }

            //Migrálás
            if (largestSpace.getReservedSpace() >= (X.getComponentVolume() - largestSpace.getSize())) {
                //Megjegyzés: egy epoch-on belül egy komponenesben minden node ugyanabban a klaszterben van
                migrate(newComponent,c.getId());
                Integer reservedSpace = largestSpace.getReservedSpace()
                        - (X.getComponentVolume() - largestSpace.getSize());
                if(reservedSpace < 0){
                    System.out.println("");
                }
                //Komponens helyfoglalásának beállítása
                newComponent.setReservedSpace(reservedSpace);
                //Klaszter helyfoglalásának beállítása
                c.setReservedSpace(reservedSpace);
            } else {
                Integer spareRate = Math.min(graph.getThreshold(), 2 * newComponent.getSize());
                Integer minReservedSpace = Math.min(graph.getThreshold() - newComponent.getSize(), newComponent.getSize());
                if(minReservedSpace < 0){
                    System.out.println("");
                }
                c = findSpareCluster(spareRate);
                migrate(newComponent,c.getId());
                //Komponens helyfoglalásának beállítása
                newComponent.setReservedSpace(minReservedSpace);
                //Klaszter helyfoglalásának beállítása
                c.setReservedSpace(minReservedSpace);
            }

            //Új komponens hozzádása a komponenslistához
            graph.addComponent(newComponent);

            //A X-beli komponensek id-i
            Set<UUID> oldComponentIds = X.getComponentSet().stream().map(Component::getId).collect(Collectors.toSet());

            //A X-beli komponensek éleinek frissítése
            Set<ComponentEdge> spareEdges = graph.getEdges().stream()
                    .filter(componentEdge -> oldComponentIds.contains(componentEdge.getSrcComponentId()) &&
                    oldComponentIds.contains(componentEdge.getDstComponentId())).collect(Collectors.toSet());
            graph.getEdges().stream().forEach(componentEdge -> {
                componentEdge.setSrcComponentId(newComponent.getId());
                componentEdge.setDstComponentId(newComponent.getId());
            });

            //Az X-en belüli komponensek törlése
            graph.getComponentList().stream().filter(component -> oldComponentIds.contains(component))
                    .forEach(component -> graph.getComponentList().remove(component));
        }
    }

    public void migrate(Component component, UUID clusterId) {
        Cluster newCluster = findCluster(clusterId);
        component.getNodes().forEach(node -> {
            //Eltávolítja a jelenlegi klazterből
            Cluster oldCluster = findCluster(node.getClusterId());
            //while(oldCluster.isFull())
            if(!oldCluster.getId().equals(newCluster.getId())){
                //Hozzáadja az új klaszterhez
                newCluster.getNodes().add(node);
                node.setClusterId(clusterId);

                oldCluster.getNodes().remove(node);
            }
        });
    }

    public Cluster findCluster(UUID clusterId) {
        return clusters.stream().filter(cluster -> cluster.getId().equals(clusterId)).findFirst().orElse(null);
    }

    public Cluster findSpareCluster(Integer spareRate) {
        List<Integer> sList = testSpare();
       Cluster c = clusters.stream()
               .filter(cluster -> !cluster.isFull() && cluster.getSpareSpace() >= spareRate).findFirst().orElse(null);
        if (c == null){
            System.out.println("");
        }
        return c;
    }

    public List<Integer> testSpare(){
        List<Integer> sList = new ArrayList<>();
        clusters.stream()
                .forEach(cluster -> sList.add(cluster.getSpareSpace()));
        return sList;
    }

    public void split() {
        ComponentSet Y = graph.getY();
        if (Y.getComponentVolume() != 0) {
            //Minden komponens vágása Y-ból
            Y.getComponentSet().stream().filter(component -> !component.isSingleton())
                    .forEach(component -> {
                component.getNodes().stream().forEach(node -> {
                    //Új singleton komponensek létrehozása
                    List singletonComponent = new ArrayList<Node>();
                    singletonComponent.add(node);
                    graph.addComponent(new Component(singletonComponent));

                    //Élek frissíése a singleton komponensek szerint
                    node.getRequests().stream()
                            .forEach(request -> graph.addEdge(new ComponentEdge(request.getSrc(), request.getDst(),
                                    //TODO: check
                                    node.getRequests().get(0).getSrc(),node.getRequests().get(0).getDst(),0)));

                    //Régi élek törlése, ahol az src Y-beli
                    Set<ComponentEdge> oldEdges = graph.getEdges()
                            .stream().filter(componentEdge -> componentEdge.getSrcComponentId() == component.getId())
                            .collect(Collectors.toSet());
                    graph.getEdges().forEach(componentEdge -> {
                        if(oldEdges.contains(componentEdge.getSrcComponentId())){
                            graph.removeEdge(componentEdge);
                        }
                    });
                });

                //Régi komponens törlése
                graph.getComponentList().remove(graph.getComponentByComponentId(component.getId()));
            });

            //reset the weights of all edges involving at least one newly created component.
            //TODO: mindnek, vagy csak Y-on belül?
            //graph.getEdges().stream().forEach(componentEdge -> componentEdge.setWeight(0));

            //If necessary, migrate at most vol(Y)/2+1 singletons to clusters with spare space
            if (checkSpareSpace()) {
                Integer atMostSingleton = Y.getComponentVolume() / 2 + 1;
                List<Component> singletonComponents = graph.getComponentList().stream()
                        .filter(component -> component.isSingleton()).collect(Collectors.toList());

                for (Integer i = 0; getMaxSpareSpace() < threshold || i < atMostSingleton - 1; i++) {
                    migrate(singletonComponents.get(i),getMaxSpareSpaceCluster().getId());
                }
            }

            if (checkSpareSpace()) {
                throw new UnsupportedOperationException();
            }
        }

        //TODO check: addNode létezik legalább 1 olyan klaszter, ahol van clusterSize/2 szabad hely
        //TODO threshold/costs
    }

    //Teljesíti-e, hogy van olyan klaszter, aminek létezik spareSpace(s)>=k
    Boolean checkSpareSpace() {
        return clusters.stream().max((o1, o2) -> Integer.compare(o1.getSpareSpace(), o2.getSpareSpace()))
                .filter(cluster -> cluster.getSpareSpace() >= threshold)
                .isPresent();
    }

    Integer getMaxSpareSpace() {
        return threshold - clusters.stream()
                .max((o1, o2) -> Integer.compare(o1.getSpareSpace(), o2.getSpareSpace()))
                .map(Cluster::getSpareSpace)
                .orElse(null);
    }

    Cluster getMaxSpareSpaceCluster(){
        return clusters.stream()
                .max((o1, o2) -> Integer.compare(o1.getSpareSpace(), o2.getSpareSpace()))
                .orElse(null);
    }

    @JsonIgnore
    public List<Request> getRequests(){
        List<Request> requestList = new ArrayList<>();
        clusters.stream()
                .forEach(cluster -> cluster.getNodes().stream()
                        .forEach(node -> node.getRequests().stream()
                                .forEach(request -> requestList.add(request))));
        return requestList;
    }

    //endregion
}
