package org.abego.event.swing;

import org.abego.event.EventDispatcher;
import org.abego.event.EventService;
import org.abego.event.EventServices;
//TODO: reactivate when core test code moved to separate module
//import org.abego.event.EventTestUtil;
import org.abego.guitesting.swing.GT;
import org.abego.guitesting.swing.GuiTesting;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static javax.swing.SwingUtilities.isEventDispatchThread;

class SwingDispatcherTest {

//TODO: reactivate when core test code moved to separate module
//    @Test
//    void exceptionInObserverCodeDoesNotStopDispatching() {
//        Assertions.assertTrue(EventTestUtil.exceptionInObserverCodeDoesNotStopDispatching(
//                SwingDispatchers::newSwingDispatcher));
//    }

    @Test
    void defaultObject() {
        EventDispatcher o1 = SwingDispatchers.getDefault();
        EventDispatcher o2 = SwingDispatchers.getDefault();

        Assertions.assertSame(o1, o2);
    }
    @Test
    void observerCodeRunsInEventDispatchThread() {
        GT gt = GuiTesting.newGT();
        EventService service = EventServices.getDefault();

        EventDispatcher dispatcher = SwingDispatchers.newSwingDispatcher(service);

        service.addObserver(String.class,
                dispatcher, o -> gt.blackboard().add(String.format(
                        "%s. In EDT = %s", o, isEventDispatchThread())));
        service.postEvent("foo");

        gt.assertEqualsRetrying("foo. In EDT = true",
                () -> gt.blackboard().text());
    }


}
