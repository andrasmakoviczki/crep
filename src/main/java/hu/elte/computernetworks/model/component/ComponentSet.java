package hu.elte.computernetworks.model.component;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Andras Makoviczki on 2016. 12. 04..
 */
public class ComponentSet {
    //region fields
    private final Set<Component> componentSet;
    //endregion

    //region getter setter
    public ComponentSet() {
        this(new HashSet<>());
    }

    public ComponentSet(Set<Component> componentSet) {
        this.componentSet = componentSet;
    }

    public Set<Component> getComponentSet() {
        return componentSet;
    }
    //endregion getter setter

    //region componentSet properties
    //Komponenshalmaz számossága
    public Integer getComponentSize() {
        return componentSet.size();
    }

    //Komponenshalmaz terjedelme (node-ok száma)
    public Integer getComponentVolume() {
        return componentSet.stream().mapToInt(component -> component.getNodes().size()).sum();
    }
    //endregion
}
