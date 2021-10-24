package org.abego.event;

public interface AsyncDispatcherGroup extends AutoCloseable {

    EventDispatcher newAsyncDispatcher();

    boolean isClosed();

    Thread getThread();

    @Override
    void close();

}
