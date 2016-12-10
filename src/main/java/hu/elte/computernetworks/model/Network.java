package hu.elte.computernetworks.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import hu.elte.computernetworks.model.component.Component;
import hu.elte.computernetworks.model.component.ComponentEdge;
import hu.elte.computernetworks.model.component.ComponentGraph;
import hu.elte.computernetworks.model.component.ComponentSet;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Andras Makoviczki on 2016. 11. 27..
 */
public class Network {
    //region fields
    private final Integer threshold;
    private final List<Cluster> clusters;
    private Integer migrationCost;
    private Integer clusterSize;
    private ComponentGraph graph;
    //endregion

    //region constructor
    public Network() {
        this(new ArrayList<>());
    }

    private Network(List<Cluster> clusters) {
        this.clusters = clusters;
        this.clusterSize = 4;
        this.threshold = 2;
        this.migrationCost = 3;
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

    public Integer getMigrationCost() {
        return migrationCost;
    }

    public void setMigrationCost(Integer migrationCost) {
        this.migrationCost = migrationCost;
    }

    @JsonIgnore
    private Integer getNodeSize() {
        return clusters.stream().mapToInt(Cluster::getSize).sum();
    }

    @JsonIgnore
    public List<Request> getRequests() {
        List<Request> requestList = new ArrayList<>();
        clusters.forEach(cluster -> cluster.getNodes().forEach(node -> node.getRequests().forEach(requestList::add)));
        return requestList;
    }

    //Meghatározza minden klaszterre, hogy mennyi szabad helyet tartalmaznak
    @JsonIgnore
    private List<Integer> getSpareSpace() {
        return clusters.stream()
                .mapToInt(Cluster::getSpareSpace).boxed().collect(Collectors.toList());
    }

    //Legnagyobb szabad hellyel rendelkező klaszter
    @JsonIgnore
    private Cluster getMaxSpareSpace() {
        return clusters.stream()
                .max((o1, o2) -> Integer.compare(o1.getSpareSpace(), o2.getSpareSpace()))
                .orElse(null);
    }

    //endregion

    //region util
    //Node keresése nodeId alapján
    private Node findNodeById(UUID nodeId) {
        Node node = null;
        for (Cluster c : clusters) {
            for (Node n : c.getNodes()) {
                if (n.getId().equals(nodeId)) {
                    node = n;
                    break;
                }
            }
        }
        return node;
    }

    //Klaszter keresése clusterId alapján
    private Cluster findCluster(UUID clusterId) {
        return clusters.stream().filter(cluster -> cluster.getId().equals(clusterId)).findFirst().orElse(null);
    }

    //Megfelelő szabad hellyel rendelkező klaszter keresése
    private Cluster findSpareCluster(Integer spareRate) {
        return clusters.stream()
                .filter(cluster -> !cluster.isFull() && cluster.getSpareSpace() >= spareRate).findFirst().orElse(null);
    }

    //Összes node gyűjtése listába
    private List<Node> collectNodes() {
        List<Node> nodes = new ArrayList<>();
        clusters.stream()
                .filter(cluster -> cluster.getNodes().size() > 0)
                .forEach(cluster -> cluster.getNodes().forEach(nodes::add));
        return nodes;
    }

    //Teljesíti-e, hogy van olyan klaszter, aminek létezik spareSpace(s)>=k
    private Boolean checkSpareSpace() {
        return clusters.stream().max((o1, o2) -> Integer.compare(o1.getSpareSpace(), o2.getSpareSpace()))
                .filter(cluster -> cluster.getSpareSpace() >= threshold)
                .isPresent();
    }

    public void addCluster() {
        clusters.add(new Cluster(clusterSize));
    }

    public void addNode(Integer cluster) throws IllegalStateException, IndexOutOfBoundsException {
        if (clusters.get(cluster).getNodes().size() >= clusterSize) {
            throw new IndexOutOfBoundsException();
        }
        if (getSpareSpace().stream().mapToInt(Integer::intValue).sum() - 1 < clusterSize / 2) {
            throw new IllegalStateException();
        }
        clusters.get(cluster).addNode(new Node(clusters.get(cluster).getId()));
    }

    public void removeNode(Integer cluster) throws IndexOutOfBoundsException {
        if (clusters.get(cluster).getNodes().size() <= 0) {
            throw new IndexOutOfBoundsException();
        }
        clusters.get(cluster).removeNode();
    }

    public void removeCluster(Integer cluster) throws IllegalStateException, IndexOutOfBoundsException {
        if (clusters.size() <= 0) {
            throw new IndexOutOfBoundsException();
        }
        if (getSpareSpace().stream().mapToInt(Integer::intValue).sum() - clusters.get(cluster).getSpareSpace() < clusterSize / 2) {
            throw new IllegalStateException();
        }
        clusters.remove((int) cluster);
    }
    //endregion

    //region componentgraph
    private void ConstructGraph() {
        graph = new ComponentGraph(threshold, migrationCost);
        setGraph();
    }

    private void setGraph() {
        List<Request> requestList = getRequests();
        Component srcComponent;
        Component dstComponent;

        for (Request request : requestList) {
            Node srcNode = findNodeById(request.getSrc());
            Node dstNode = findNodeById(request.getDst());

            if (graph.findComponentByNodeId(request.getSrc()) == null) {
                List<Node> nodeList = new ArrayList<>();
                nodeList.add(srcNode);
                srcComponent = new Component(nodeList);
                graph.addComponent(srcComponent);
            } else {
                srcComponent = graph.findComponentByNodeId(request.getSrc());
            }

            if (graph.findComponentByNodeId(request.getDst()) == null) {
                List<Node> nodeList = new ArrayList<>();
                nodeList.add(dstNode);
                dstComponent = new Component(nodeList);
                graph.addComponent(dstComponent);
            } else {
                dstComponent = graph.findComponentByNodeId(request.getDst());
            }

            graph.addEdge(new ComponentEdge(srcComponent.getId(), dstComponent.getId(), srcNode.getId(), dstNode.getId(), 1));
        }
    }
    //endregion

    //region request
    public void generateRequests() throws IllegalStateException {
        if (clusters.size() > 0 && collectNodes().size() > 1) {
            resetRequests();
            RequestGenerator r = new RequestGenerator(clusters);
            r.generateRequests(getNodeSize());
        } else {
            throw new IllegalStateException();
        }
    }

    private void resetRequests() {
        clusters.forEach(cluster -> cluster.getNodes().forEach(node -> {
            node.setRequests(new ArrayList<>());
            node.setAcceptedRequests(new ArrayList<>());
        }));
        graph = null;
    }
    //endregion

    //region crep
    public void CREP() throws IllegalStateException {
        if (getRequests().size() > 0) {
            ConstructGraph();
            for (Request request : getRequests()) {
                //Keep track of communication cost
                ComponentEdge cEdge = graph.findEdge(request);
                increaseCommunicatinCost(cEdge);
                //Merge components
                merge();
                //End of a Y-epoch
                split();
            }
        } else {
            throw new IllegalStateException();
        }
    }

    private void increaseCommunicatinCost(ComponentEdge cEdge) {
        Component src = graph.getComponentByComponentId(cEdge.getSrcComponentId());
        Component dst = graph.getComponentByComponentId(cEdge.getDstComponentId());

        if (!src.getId().equals(dst.getId())) {
            cEdge.setWeight(cEdge.getWeight() + 1);
        }
    }

    private void merge() {
        ComponentSet X = graph.getX();
        if (X.getComponentSize() > 1) {
            //Komponens halmaz uniója új komponensé
            Component newComponent = new Component();
            X.getComponentSet().forEach(component -> component.getNodes().forEach(node -> newComponent.getNodes().add(node)));

            //Diszjunk komponens halmaz
            ComponentSet disjunctComponentSet = new ComponentSet(new HashSet<>(graph.getComponentList()));
            disjunctComponentSet.getComponentSet().removeAll(X.getComponentSet());

            //Azok az élek, ahol a src X-beli és dst nem X-beli
            Set<ComponentEdge> edges = graph.getEdges().stream()
                    .filter(componentEdge -> X.getComponentSet().stream()
                            .filter(component -> component.getId().equals(componentEdge.getSrcComponentId()))
                            .findFirst().isPresent()
                            && disjunctComponentSet.getComponentSet().stream()
                            .filter(component -> component.getId().equals(componentEdge.getDstComponentId()))
                            .findFirst().isPresent())
                    .collect(Collectors.toSet());

            //Csoportosítjuk végpont szerint és összegezzük az éleket
            Map<UUID, Integer> edgeDstWeightPair = edges.stream().collect(Collectors.groupingBy(ComponentEdge::getDstComponentId,
                    Collectors.summingInt(ComponentEdge::getWeight)));

            //Új élek létrehozása az új komponens alapján
            edgeDstWeightPair.entrySet().forEach(uuidIntegerEntry ->
                    graph.addEdge(
                            new ComponentEdge(
                                    newComponent.getId(), uuidIntegerEntry.getKey(),
                                    null, graph.getComponentByComponentId(uuidIntegerEntry.getKey()).getNodes().get(0).getId()
                                    , uuidIntegerEntry.getValue())));

            //Legnagyobb reservedSpace-szel rendelkző komponens kiválasztása
            Component largestSpace = X.getComponentSet().stream()
                    .filter(component -> component.getNodes().stream()
                            .filter(node -> !findCluster(node.getClusterId()).isFull())
                            .findFirst().isPresent())
                    .max((o1, o2) -> Integer.compare(o1.getReservedSpace(), o2.getReservedSpace())).orElse(null);

            //Migrálás
            if (largestSpace != null && largestSpace.getReservedSpace() >= (X.getComponentVolume() - largestSpace.getSize())) {
                //Megjegyzés: egy epoch-on belül egy komponenesben minden node ugyanabban a klaszterben van
                Cluster c = findCluster(largestSpace.getNodes().get(0).getClusterId());
                if (c != null) {
                    migrate(newComponent, c.getId());
                    Integer reservedSpace = largestSpace.getReservedSpace()
                            - (X.getComponentVolume() - largestSpace.getSize());
                    //Komponens helyfoglalásának beállítása
                    newComponent.setReservedSpace(reservedSpace);
                    //Klaszter helyfoglalásának beállítása
                    c.setReservedSpace(reservedSpace);
                }
            } else {
                Integer spareRate = Math.min(graph.getThreshold(), 2 * newComponent.getSize());
                Integer minReservedSpace = Math.min(graph.getThreshold() - newComponent.getSize(), newComponent.getSize());
                Cluster c = findSpareCluster(spareRate);
                migrate(newComponent, c.getId());
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
            graph.getEdges().forEach(componentEdge -> {
                componentEdge.setSrcComponentId(newComponent.getId());
                componentEdge.setDstComponentId(newComponent.getId());
            });

            //Az X-en belüli komponensek törlése
            List<Component> tmp = graph.getComponentList().stream().filter(component -> oldComponentIds.contains(component.getId())).collect(Collectors.toList());
            tmp.forEach(component -> graph.getComponentList().remove(component));
        }
    }

    private void migrate(Component component, UUID clusterId) {
        Cluster newCluster = findCluster(clusterId);
        component.getNodes().forEach(node -> {
            Cluster oldCluster = findCluster(node.getClusterId());
            if (!oldCluster.getId().equals(newCluster.getId())) {
                //Hozzáadja az új klaszterhez
                newCluster.getNodes().add(node);
                node.setClusterId(clusterId);
                //Régi klaszterből törli
                oldCluster.getNodes().remove(node);
            }
        });
    }

    private void split() {
        ComponentSet Y = graph.getY();
        if (Y.getComponentVolume() != 0) {
            //Minden komponens vágása Y-ból
            Y.getComponentSet().stream().filter(component -> !component.isSingleton())
                    .forEach(component -> {
                        component.getNodes().forEach(node -> {
                            //Új singleton komponensek létrehozása
                            List<Node> singletonComponent = new ArrayList<>();
                            singletonComponent.add(node);
                            graph.addComponent(new Component(singletonComponent));

                            //Élek frissíése a singleton komponensek szerint
                            node.getRequests().forEach(request -> graph.addEdge(new ComponentEdge(request.getSrc(), request.getDst(),
                                    node.getRequests().get(0).getSrc(), node.getRequests().get(0).getDst(), 0)));
                        });

                        //Régi élek törlése, ahol az src Y-beli
                        Set<ComponentEdge> oldEdges = graph.getEdges().stream()
                                .filter(componentEdge -> Y.getComponentSet().stream()
                                        .filter(c -> c.getId() == componentEdge.getSrcComponentId())
                                        .findFirst().isPresent())
                                .collect(Collectors.toSet());
                        oldEdges.forEach(componentEdge -> graph.removeEdge(componentEdge));

                        //Régi komponens törlése
                        graph.getComponentList().remove(graph.getComponentByComponentId(component.getId()));
                    });

            //Szükség esetén legfeljebb vol(Y)/2+1 signleton áthelyezése
            if (checkSpareSpace()) {
                Integer atMostSingleton = Y.getComponentVolume() / 2 + 1;
                List<Component> singletonComponents = graph.getComponentList().stream()
                        .filter(Component::isSingleton).collect(Collectors.toList());

                for (Integer i = 0; getMaxSpareSpace().getSpareSpace() < threshold || i < atMostSingleton - 1; i++) {
                    migrate(singletonComponents.get(i), getMaxSpareSpace().getId());
                }
            }
        }
    }
    //endregion
}
