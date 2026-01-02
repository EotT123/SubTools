package org.lodder.subtools.multisubdownloader.framework.event;

import java.util.Collection;

import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class Event {
    @val String eventName;
    private final EventBag eventBag;

    public Event(String name, EventBag bag=new EventBag()) {
        this.eventName = name;
        this.eventBag = bag;
    }

    public Object getAttribute(String name) {
        return this.eventBag.getAttribute(name);
    }

    public Collection<String> getAttributeNames() {
        return this.eventBag.getAttributeNames();
    }

    public void setAttribute(String name, Object object) {
        this.eventBag.setAttribute(name, object);
    }

}
