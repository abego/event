package org.abego.event.swing;

import org.abego.event.EventDispatcher;
import org.abego.event.EventService;
import org.abego.event.EventServices;
import org.abego.event.ExplicitDispatcher;

import javax.swing.SwingUtilities;
import java.util.function.Consumer;

final class SwingDispatcher implements EventDispatcher {
    private static final SwingDispatcher DEFAULT =
            new SwingDispatcher(EventServices.getDefault());
    private final ExplicitDispatcher dispatcher;

    private SwingDispatcher(EventService eventService) {
        dispatcher = eventService.newExplicitDispatcher(d -> onDispatchPending());
    }

    public static EventDispatcher getDefault() {
        return DEFAULT;
    }

    public static EventDispatcher newSwingDispatcher(EventService eventService) {
        return new SwingDispatcher(eventService);
    }

    @Override
    public void process(Object event, Iterable<Consumer<Object>> listeners) {
        dispatcher.process(event, listeners);
    }

    private void onDispatchPending() {
        SwingUtilities.invokeLater(dispatcher::dispatch);
    }
}
