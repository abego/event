package org.abego.event;

import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.abego.event.EventUtil.getDispatchErrorMessageSupplier;

final class DirectDispatcher implements EventDispatcher {
    private static final Logger LOGGER = Logger.getLogger(DirectDispatcher.class.getName());

    private static final DirectDispatcher DEFAULT = new DirectDispatcher();

    private DirectDispatcher() {
    }

    public static EventDispatcher getDefault() {
        return DEFAULT;
    }

    @Override
    public void process(Object event, Iterable<Consumer<Object>> listeners) {
        listeners.forEach(listener -> {
            try {
                listener.accept(event);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e,
                        getDispatchErrorMessageSupplier(event, listener, e));
            }
        });
    }
}
