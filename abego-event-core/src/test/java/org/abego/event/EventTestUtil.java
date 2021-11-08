package org.abego.event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class EventTestUtil {

    public static EventWithSource newEventWithSource(String source, String text) {
        return new EventWithSource() {
            @Override
            public Object getSource() {
                return source;
            }

            @Override
            public String toString() {
                return text;
            }
        };
    }

    public static boolean exceptionInObserverCodeDoesNotStopDispatching(
            Function<EventService, EventDispatcher> dispatchFactory) {

        // Exception while dispatching does not stop dispatching
        List<String> result = new ArrayList<>();
        EventService eventService = EventServices.newEventService();
        EventDispatcher dispatcher = dispatchFactory.apply(eventService);
        eventService.addObserver(String.class, dispatcher, result::add);
        eventService.addObserver(String.class, dispatcher,
                s -> {
                    if (s.equals("bar"))
                        throw new IllegalArgumentException();
                });

        eventService.postEvent("foo");
        eventService.postEvent("bar"); // will cause exception in observer code
        eventService.postEvent("baz");

        // even with exception all events are observed
        assertEquals("foo\nbar\nbaz", String.join("\n", result));

        return true;
    }
}
