package org.abego.event;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.abego.event.EventUtil.getDispatchErrorMessageSupplier;

final class ExplicitDispatcherImpl implements ExplicitDispatcher {
    private static final Logger LOGGER = Logger.getLogger(ExplicitDispatcherImpl.class.getName());
    private final AtomicBoolean dispatchPending = new AtomicBoolean();
    private final Queue<Item> queue = new ConcurrentLinkedQueue<>();
    private final Consumer<ExplicitDispatcher> onDispatchPending;

    private static class Item {
        final Object event;
        final Iterable<Consumer<Object>> listeners;

        private Item(Object event, Iterable<Consumer<Object>> listeners) {
            this.event = event;
            this.listeners = listeners;
        }
    }

    private ExplicitDispatcherImpl(Consumer<ExplicitDispatcher> onDispatchPending) {
        this.onDispatchPending = onDispatchPending;
    }

    public static ExplicitDispatcherImpl newExplicitDispatcherImpl(Consumer<ExplicitDispatcher> onDispatchPending) {
        return new ExplicitDispatcherImpl(onDispatchPending);
    }

    @Override
    public void process(Object event, Iterable<Consumer<Object>> listeners) {
        queue.add(new Item(event, listeners));

        if (!dispatchPending.getAndSet(true)) {
            onDispatchPending.accept(this);
        }
    }

    @Override
    public boolean isDispatchPending() {
        return dispatchPending.get();
    }

    @Override
    public void dispatch() {
        dispatchPending.set(false);

        Item item;
        while ((item = queue.poll()) != null) {
            Object event = item.event;
            for (Consumer<Object> listener : item.listeners) {
                try {
                    listener.accept(event);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, e,
                            getDispatchErrorMessageSupplier(event, listener, e));
                }
            }
        }
    }

}
