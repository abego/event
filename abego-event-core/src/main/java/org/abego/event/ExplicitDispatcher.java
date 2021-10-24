package org.abego.event;

public interface ExplicitDispatcher extends EventDispatcher {

    boolean isDispatchPending();
    void dispatch();
}
