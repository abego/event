package org.abego.event;

import java.util.function.Consumer;

public interface EventDispatcher {
    //TODO why not call this method "dispatch"
    void process(Object event, Iterable<Consumer<Object>> listeners);
}
