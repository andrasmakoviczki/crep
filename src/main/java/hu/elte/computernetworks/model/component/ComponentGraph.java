package hu.elte.computernetworks.model.component;

import hu.elte.computernetworks.model.Request;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Andras Makoviczki on 2016. 11. 28..
 */
public class ComponentGraph {
    //region fields
    private final List<Component> componentList;
    private final List<ComponentEdge> edges;
    private final Integer threshold;
    private final Integer migrationCost;
    //endregion

    //region constructor
    public ComponentGraph(Integer threshold, Integer migrationCost) {
        this.componentList = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.threshold = threshold;
        this.migrationCost = migrationCost;
    }
    //endregion

    //region getter setter
    public List<Component> getComponentList() {
        return componentList;
    }

    public List<ComponentEdge> getEdges() {
        return edges;
    }

    public Integer getThreshold() {
        return threshold;
    }

    //Visszaadja a komponensId-hez a hozzátartozó komponenst
    public Component getComponentByComponentId(UUID componentId) {
        Component component = null;
        for (Component c : componentList) {
            if (c.getId().equals(componentId)) {
                component = c;
                break;
            }
        }
        return component;
    }

    //Kommunikációs költség
    private Integer getCommunicationCosts(ComponentSet cSet) {
        /*Predicate<UUID> inSet = x ->
                cSet.getComponentSet().stream().anyMatch(component -> component.getId() == x);*/

        Set<UUID> inSet = cSet.getComponentSet().stream().map(Component::getId).collect(Collectors.toSet());
        //azok az élek, amiknek a megadott komponenshalmazban van a source-uk
        return edges.stream().filter(edge -> inSet.contains(edge.getSrcComponentId()))
                //vesszük a súlyokat
                .mapToInt(ComponentEdge::getWeight)
                //összeadjuk
                .sum();
    }

    //Legnagyobb számosságú komponenshalmaz, amire érvényes vol(X) <= k && com(X) >= (|X|-1)*ALPHA
    public ComponentSet getX() {
        ComponentSet cSet = new ComponentSet();
        ComponentSet cSetTmp = new ComponentSet();

        //növekvő sorrendbe rendezi a komponenseket volume szerint
        Iterator<Component> componentIterator = componentList.stream().sorted((o1, o2) -> Integer.compare(o1.getNodes().size(), o2.getNodes().size()))
                .iterator();

        //amíg teljesülnek a feltételek növeli a halmazt
        while (componentIterator.hasNext()) {
            Component c = componentIterator.next();
            cSetTmp.getComponentSet().add(c);
            //vol(X) <= k && com(X) >= (|X|-1)*ALPHA
            if (cSetTmp.getComponentVolume() <= threshold &&
                    getCommunicationCosts(cSetTmp) >= (cSetTmp.getComponentSize() - 1) * migrationCost) {
                cSet.getComponentSet().add(c);
            } else {
                break;
            }
        }
        return cSet;
    }

    //Legkisebb számosságú komponenshalmaz, amire érvényes, hogy vol(Y) > k && com(Y) >= vol(Y)*ALPHA
    public ComponentSet getY() {
        ComponentSet cSet = new ComponentSet(new HashSet<>());
        ComponentSet cSetTmp = new ComponentSet(new HashSet<>());

        //csökkenő sorrendbe rendezi a komponenseket volume szerint
        Comparator<Component> componentComparator = (o1, o2) -> Integer.compare(o1.getNodes().size(), o2.getNodes().size());
        Iterator<Component> componentIterator = componentList.stream()
                .sorted(componentComparator.reversed())
                .iterator();

        //amíg teljesülnek a feltételek csökkenti a halmazt
        while (componentIterator.hasNext()) {
            Component c = componentIterator.next();
            cSetTmp.getComponentSet().add(c);
            //vol(Y) > k && com(Y) >= vol(Y)*ALPHA
            if (cSetTmp.getComponentVolume() > threshold &&
                    getCommunicationCosts(cSetTmp) >= cSetTmp.getComponentVolume() * migrationCost) {
                cSet.getComponentSet().add(c);
            } else {
                break;
            }
        }
        return cSet;
    }
    //endregion

    //region util
    //Megkeres egy élt egy request alapján
    public ComponentEdge findEdge(Request request) {
        ComponentEdge cEdge = null;
        for (ComponentEdge e : edges) {
            if ((e.getSrcNodeId() == null || e.getSrcNodeId().equals(request.getSrc())) && e.getDstNodeId().equals(request.getDst())) {
                cEdge = e;
            }
        }
        return cEdge;
    }

    //Megkeresi a node-hoz tartozó komponenst
    public Component findComponentByNodeId(UUID nodeId) {
        return componentList.stream()
                .filter(component -> component.getNodes().stream()
                        .filter(node -> node.getId().equals(nodeId))
                        .findFirst().isPresent())
                .findFirst().orElse(null);
    }

    public void addEdge(ComponentEdge edge) {
        edges.add(edge);
    }

    public void removeEdge(ComponentEdge edge) {
        edges.remove(edge);
    }

    public void addComponent(Component component) {
        componentList.add(component);
    }
    //endregion

}
