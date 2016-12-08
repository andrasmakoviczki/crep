package hu.elte.computernetworks.model.component;

import hu.elte.computernetworks.model.Node;
import hu.elte.computernetworks.model.Request;
import org.apache.log4j.lf5.util.StreamUtils;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by Andras Makoviczki on 2016. 11. 28..
 */
public class ComponentGraph {
    //region fields
    private List<Component> componentList;
    private List<ComponentEdge> edges;
    private Integer threshold;
    private Integer migrationCost;
    private Integer DEFAULT_THRESHOLD = 5;
    private Integer DEFAULT_MIGRATION_COST = 3;
    //endregion

    //region constructor
    public ComponentGraph(List<Node> nodes) {
        this.componentList = new ArrayList<>();//setComponents(nodes);
        this.edges = new ArrayList<>();//setEdges(componentList);
        this.threshold = DEFAULT_THRESHOLD;
        this.migrationCost = DEFAULT_MIGRATION_COST;
    }
    //endregion

    //region getter setter
    public List<Component> getComponentList() {
        return componentList;
    }

    public void setComponentList(List<Component> componentList) {
        this.componentList = componentList;
    }

    public List<ComponentEdge> getEdges() {
        return edges;
    }

    public Integer getThreshold() {
        return threshold;
    }

    public void setEdges(List<ComponentEdge> edges) {
        this.edges = edges;
    }

    public void setThreshold(Integer threshold) {
        this.threshold = threshold;
    }

    //endregion

    //Minden node-hoz létrehoz egy új komponenst, majd a komponensbe node listájába teszi az elemet
    public List<Component> setComponents(List<Node> nodes) {
        List<Component> components = new ArrayList<Component>();
        nodes.forEach(node -> {
            List<Node> nodeList = new ArrayList<Node>();
            nodeList.add(node);
            components.add(new Component(nodeList));
        });
        return components;
    }

    //Minden komponens node listája alapján létrehozza az éleket
    //public List<ComponentEdge> setEdges(List<Component> componentList) {
       // List<ComponentEdge> edges = new ArrayList<ComponentEdge>();
        /*componentList.stream().forEach(component -> component.getNodes().stream()
                .forEach(node -> node.getRequests().stream()
                        .forEach(request -> edges.add(new ComponentEdge(
                        component.getId(), findComponentByNodeId(request.getDst()).getId(),
                        node.getRequests().get(0).getSrc(),node.getRequests().get(0).getDst(),0)
                )))
        );*/
        /*List<ComponentEdge> edges = new ArrayList<ComponentEdge>();
        componentList.stream().forEach(component -> component.getNodes().stream()
                .forEach(node -> node.getRequests().stream()
                        .forEach(request -> {
                            List<Node> nList = new ArrayList<Node>();
                            nList.add(request.getDst())
                            new Component()
                        })
                );*/
    /*    return edges;
    }*/

    //Megkeresi a komponensId-hez a hozzátartozó komponenst
    public Component getComponentByComponentId(UUID componentId){
        Component component = null;
        for (Component c: componentList) {
            if(c.getId().equals(componentId)){
                component = c;
                break;
            }
        }

        if(component == null){
            System.out.println("");
        }
        return component;
        //return componentList.stream().filter(component -> ).findFirst().orElse(null);
    }

    //Megkeresi a node-hoz tartozó komponenst
    public Component findComponentByNodeId(UUID nodeId) {
        Component c = null;

        Boolean l = false;
        for (Integer i = 0; !l && i < componentList.size(); i++) {
            Component component = componentList.get(i);
            for (int j = 0; !l && j < component.getNodes().size(); j++) {
                Node node = component.getNodes().get(j);
                if (node.getId().equals(nodeId)) {
                    c = component;
                    l = true;
                }
            }
        }

        return c;
        //TODO check
        /*return componentList.stream()
                .filter(component -> component.getNodes().stream()
                .anyMatch(node -> node.getId() == nodeId)).findFirst().orElse(null);*/

    }

    //TODO élek karbantartása migráció után
    //Kommunikációs költség
    public Integer getCommunicationCosts(ComponentSet cSet) {
        /*Predicate<UUID> inSet = x ->
                cSet.getComponentSet().stream().anyMatch(component -> component.getId() == x);*/

        Set<UUID> inSet = cSet.getComponentSet().stream().map(Component::getId).collect(Collectors.toSet());
        //azok az élek, amiknek a megadott komponenshalmazban van a source-uk
        return edges.stream().filter(edge -> inSet.contains(edge.getSrcComponentId()))
                //vesszük a súlyokat
                .mapToInt(edge -> edge.getWeight())
                //összeadjuk
                .sum();
    }

    public ComponentSet getX(){
        ComponentSet cSet = new ComponentSet();
        ComponentSet cSetTmp = new ComponentSet();

        //Növekvő sorrendbe rendezi a komponenseket volume szerint
        Iterator<Component> componentIterator = componentList.stream().sorted((o1, o2) -> Integer.compare(o1.getNodes().size(),o2.getNodes().size()))
                .iterator();

        //amíg teljesülnek a feltételek növeli a halmazt
        while (componentIterator.hasNext()){
            Component c = componentIterator.next();
            cSetTmp.getComponentSet().add(c);
            Integer t = threshold;
            Integer cvol = cSetTmp.getComponentVolume();
            Integer ccom = getCommunicationCosts(cSetTmp);
            //vol(X) <= k && com(X) >= (|X|-1)*ALPHA
            if(cSetTmp.getComponentVolume() <= threshold &&
                    getCommunicationCosts(cSetTmp) >= (cSetTmp.getComponentSize() - 1) * migrationCost){
                cSet.getComponentSet().add(c);
            } else {
                cvol = cSetTmp.getComponentVolume();
                ccom = getCommunicationCosts(cSetTmp);
                break;
            }
        }
        Integer cvol = cSet.getComponentVolume();
        Integer ccom = getCommunicationCosts(cSet);
        Integer cvoltmp = cSetTmp.getComponentVolume();
        Integer ccomtmp = getCommunicationCosts(cSetTmp);

        return cSet;
    }

    public ComponentSet getY(){
        ComponentSet cSet = new ComponentSet(new HashSet<>());
        ComponentSet cSetTmp = new ComponentSet(new HashSet<>());

        //Csökkenő sorrendbe rendezi a komponenseket volume szerint
        Comparator<Component> componentComparator = (o1,o2)->Integer.compare(o1.getNodes().size(),o2.getNodes().size());
        Iterator<Component> componentIterator = componentList.stream()
                .sorted(componentComparator.reversed())
                .iterator();

        //amíg teljesülnek a feltételek csökkenti a halmazt
        while (componentIterator.hasNext()){
            Component c = componentIterator.next();
            cSetTmp.getComponentSet().add(c);
            Integer t = threshold;
            Integer cvol = cSetTmp.getComponentVolume();
            Integer ccom = getCommunicationCosts(cSetTmp);

            //vol(Y) > k && com(Y) >= vol(Y)*ALPHA
            if(cSetTmp.getComponentVolume() > threshold &&
                    getCommunicationCosts(cSetTmp) >= cSetTmp.getComponentVolume() * migrationCost){
                cSet.getComponentSet().add(c);
            } else {
                break;
            }
        }

        return cSet;
    }

    public void addEdge(ComponentEdge edge){
        edges.add(edge);
    }

    public void removeEdge(ComponentEdge edge){
        edges.remove(edge);
    }

    public ComponentEdge findEdge(Request request){
        ComponentEdge cEdge = null;
        for (ComponentEdge e: edges) {
            UUID esrcid = e.getSrcNodeId();
            UUID edstid = e.getDstNodeId();
            UUID rsrcid = request.getSrc();
            UUID rdstid = request.getDst();
            if (e.getSrcNodeId().equals(request.getSrc()) && e.getDstNodeId().equals(request.getDst())){
                cEdge = e;
            }
        }

        if(cEdge == null){
            System.out.println("");
        }
        return cEdge;
        /*return edges.stream().filter(componentEdge ->
                (componentEdge.getSrcNodeId().contains(request.getSrc()) && componentEdge.getDstNodeId() == request.getDst())
                        || (componentEdge.getSrcNodeId().contains(request.getDst()) && componentEdge.getDstNodeId() == request.getSrc()))
                .findFirst()
                .orElse(null);*/
    }

    public void addComponent(Component component){
        componentList.add(component);
    }
}
