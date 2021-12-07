package org.abego.event;

import org.junit.jupiter.api.Test;

import static org.abego.commons.test.AssertRetrying.assertEqualsRetrying;
import static org.abego.commons.test.AssertRetrying.assertTrueRetrying;

class AsyncDispatcherGroupTest {

    @Test
    void exceptionInObserverCodeDoesNotStopDispatching() {
        // Same code in SwingDispatcherTest, but different "dispatcher".

        @SuppressWarnings("StringBufferMayBeStringBuilder") // wrong warning, output used by 2 threads
        StringBuffer output = new StringBuffer(); // thread-safe StringBuffer
        EventService eventService = EventServices.newEventService();
        EventDispatcher dispatcher = eventService.newAsyncEventDispatcherGroup()
                .newAsyncDispatcher();

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
    void smoketest() {
        @SuppressWarnings("StringBufferMayBeStringBuilder") // wrong warning, output used by 2 threads
        StringBuffer output = new StringBuffer(); // thread-safe StringBuffer
        EventService eventService = EventServices.newEventService();
        AsyncDispatcherGroup group = eventService.newAsyncEventDispatcherGroup();

        EventDispatcher dispatcher = group.newAsyncDispatcher();
        eventService.addObserver(String.class, dispatcher, output::append);

        eventService.postEvent("foo");
        eventService.postEvent("bar");
        group.close();
        eventService.postEvent("baz");

        assertEqualsRetrying("foobar", output::toString);
    }

    @Test
    void interruptingThreadWillCloseGroup() {
        @SuppressWarnings("StringBufferMayBeStringBuilder") // wrong warning, output used by 2 threads
        StringBuffer output = new StringBuffer(); // thread-safe StringBuffer
        EventService eventService = EventServices.newEventService();

        AsyncDispatcherGroup group = eventService.newAsyncEventDispatcherGroup();
        EventDispatcher dispatcher = group.newAsyncDispatcher();
        eventService.addObserver(String.class, dispatcher, output::append);

        eventService.postEvent("foo");
        eventService.postEvent("bar");
        // interrupting the dispatching thread closes the group
        group.getThread().interrupt();

        assertTrueRetrying(group::isClosed);

        assertEqualsRetrying("foobar", output::toString);
    }
}