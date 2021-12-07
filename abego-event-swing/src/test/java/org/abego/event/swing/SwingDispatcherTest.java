package org.abego.event.swing;

import org.abego.event.EventDispatcher;
import org.abego.event.EventService;
import org.abego.event.EventServices;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static javax.swing.SwingUtilities.isEventDispatchThread;
import static org.abego.commons.test.AssertRetrying.assertEqualsRetrying;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SwingDispatcherTest {

    @Test
    void exceptionInObserverCodeDoesNotStopDispatching() {
        // Same code in AsyncDispatcherGroupTest, but different "dispatcher".

        @SuppressWarnings("StringBufferMayBeStringBuilder") // wrong warning, output used by 2 threads
        StringBuffer output = new StringBuffer(); // thread-safe StringBuffer
        EventService eventService = EventServices.newEventService();
        EventDispatcher dispatcher =
                SwingDispatchers.newSwingDispatcher(eventService);

        // Exception while dispatching does not stop dispatching
        eventService.addObserver(String.class, dispatcher, output::append);
        eventService.addObserver(String.class, dispatcher,
                s -> {
                    if (s.equals("bar"))
                        throw new IllegalArgumentException();
                });

        eventService.postEvent("foo");
        eventService.postEvent("bar"); // will cause exception in observer code
        eventService.postEvent("baz");

        // even with exception all events are observed
        assertEqualsRetrying("foobarbaz", output::toString);
    }

    @Test
    void constructor() {
        assertThrows(
                UnsupportedOperationException.class, SwingDispatchers::new);
    }

    @Test
    void defaultObject() {
        EventDispatcher o1 = SwingDispatchers.getDefault();
        EventDispatcher o2 = SwingDispatchers.getDefault();

        Assertions.assertSame(o1, o2);
    }

    @Test
    void observerCodeRunsInEventDispatchThread() {
        StringBuffer output = new StringBuffer(); // thread-safe StringBuffer
        EventService service = EventServices.getDefault();
        EventDispatcher dispatcher = SwingDispatchers.newSwingDispatcher(service);

        service.addObserver(String.class,
                dispatcher, o -> output.append(String.format(
                        "%s. In EDT = %s", o, isEventDispatchThread())));
        service.postEvent("foo");

        assertEqualsRetrying("foo. In EDT = true", output::toString);
    }


}
