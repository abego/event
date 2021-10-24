package org.abego.event.swing;

import org.abego.event.EventDispatcher;
import org.abego.event.EventService;

public final class SwingDispatchers {
    SwingDispatchers() {
        throw new UnsupportedOperationException();
    }

    public static EventDispatcher getDefault() {
        return SwingDispatcher.getDefault();
    }

    public static EventDispatcher newSwingDispatcher(EventService eventService) {
        return SwingDispatcher.newSwingDispatcher(eventService);
    }

}
