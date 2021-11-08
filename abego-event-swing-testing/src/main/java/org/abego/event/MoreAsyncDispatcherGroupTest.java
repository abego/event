package org.abego.event;

import org.abego.guitesting.swing.GT;
import org.abego.guitesting.swing.GuiTesting;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * See AsyncDispatcherGroupTest in test sources of abego-event-core
 */
public class MoreAsyncDispatcherGroupTest {
    private final GT gt = GuiTesting.newGT();

    @Test
    void exceptionInObserverCodeDoesNotStopDispatching() {
        assertTrue(AsyncEventTestUtil.exceptionInObserverCodeDoesNotStopDispatchingWithAsyncSupport(
                eventService -> eventService.newAsyncEventDispatcherGroup().newAsyncDispatcher()));
    }

    @Test
    void smoketest() {
        EventService eventService = EventServices.newEventService();
        AsyncDispatcherGroup group = eventService.newAsyncEventDispatcherGroup();
        EventDispatcher dispatcher = group.newAsyncDispatcher();
        eventService.addObserver(String.class, dispatcher,
                s -> gt.blackboard().add(s));

        eventService.postEvent("foo");
        eventService.postEvent("bar");
        group.close();
        eventService.postEvent("baz");

        gt.assertEqualsRetrying("foo\nbar", () -> gt.blackboard().text());
    }

    @Test
    void interruptingThreadWillCloseGroup() {
        EventService eventService = EventServices.newEventService();
        AsyncDispatcherGroup group = eventService.newAsyncEventDispatcherGroup();
        EventDispatcher dispatcher = group.newAsyncDispatcher();
        eventService.addObserver(String.class, dispatcher,
                s -> gt.blackboard().add(s));

        eventService.postEvent("foo");
        eventService.postEvent("bar");
        // interrupting the dispatching thread closes the group
        group.getThread().interrupt();

        gt.assertTrueRetrying(group::isClosed);

        gt.assertEqualsRetrying("foo\nbar", () -> gt.blackboard().text());
    }
}