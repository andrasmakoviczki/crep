package hu.elte.computernetworks.model.component;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Andras Makoviczki on 2016. 12. 04..
 */
public class ComponentSet {
    private UUID id;
    private Set<Component> componentSet;

    public ComponentSet(){
        this(new HashSet<>());
    }

    public ComponentSet(Set<Component> componentSet) {
        this.id = UUID.randomUUID();
        this.componentSet = componentSet;
    }

    public Set<Component> getComponentSet() {
        return componentSet;
    }

    public void setComponentSet(Set<Component> componentSet) {
        this.componentSet = componentSet;
    }

    //Komponenshalmaz számossága
    public Integer getComponentSize(){
        return componentSet.size();
    }

    //Komponenshalmaz terjedelme (node-ok száma)
    public Integer getComponentVolume(){
        return componentSet.stream().mapToInt(component -> component.getNodes().size()).sum();
    }

    //
    public Component getLargestReservedSpace(){
        return componentSet.stream()
                .max((o1, o2) -> Integer.compare(o1.getReservedSpace(),o2.getReservedSpace())).orElse(null);
    }
}
