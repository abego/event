package org.abego.event;

import org.abego.guitesting.swing.GT;
import org.abego.guitesting.swing.GuiTesting;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DirectDispatcherTest {
    @Test
    void exceptionInObserverCodeDoesNotStopDispatching() {
        assertTrue(EventTestUtil.exceptionInObserverCodeDoesNotStopDispatching(
                EventService::getDirectDispatcher));
    }
}
