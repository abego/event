package org.abego.event;

import org.abego.guitesting.swing.GT;
import org.abego.guitesting.swing.GuiTesting;

import java.util.function.Function;

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
            Function<EventService,EventDispatcher> dispatchFactory) {

        // Exception while dispatching does not stop dispatching
        GT gt = GuiTesting.newGT();
        EventService eventService = EventServices.newEventService();
        EventDispatcher dispatcher = dispatchFactory.apply(eventService);
        eventService.addObserver(String.class, dispatcher,
                s -> gt.blackboard().add(s));
        eventService.addObserver(String.class, dispatcher,
                s -> {
                    if (s.equals("bar"))
                        throw new IllegalArgumentException();
                });

        eventService.postEvent("foo");
        eventService.postEvent("bar"); // will cause exception in observer code
        eventService.postEvent("baz");

        // even with exception all events are observed
        gt.assertEqualsRetrying("foo\nbar\nbaz", () -> gt.blackboard().text());

        return true;
    }
}
