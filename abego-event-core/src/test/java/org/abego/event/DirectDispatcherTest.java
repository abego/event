package org.abego.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DirectDispatcherTest {
    @Test
    void exceptionInObserverCodeDoesNotStopDispatching() {
        assertTrue(EventTestUtil.exceptionInObserverCodeDoesNotStopDispatching(
                EventService::getDirectDispatcher));
    }
}
