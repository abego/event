package org.abego.event;

import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.abego.event.AsyncDispatcherGroupDefault.newAsyncEventDispatcherGroupDefault;
import static org.abego.event.EventObserverDefault.newEventObserverDefault;
import static org.abego.event.ExplicitDispatcherImpl.newExplicitDispatcherImpl;

//TODO: support debugging. E.g.
//  - list registered observers (incl. source code location)
//  - for PropertyChange events check property name
//    (property must exist in source of postEvent/addObserver, @Events annotated)
//       (special handling when no source defined with addObserver)
//  - log events (post, pass to dispatcher, dispatching)
//  - "dependency graph" (posts <-> observers)


final class EventServiceDefault implements EventService {
    //region Construction
    private static final EventService DEFAULT_INSTANCE = newInstance();

    private EventServiceDefault() {
    }

    public static EventService getDefault() {
        return DEFAULT_INSTANCE;
    }

    public static EventService newInstance() {
        return new EventServiceDefault();
    }

    //endregion
    //region Posting Events
    @Override
    public void postEvent(Object event) {
        checkNotClosed();

        Map<EventDispatcher, List<Consumer<Object>>> dispatchersToListeners = null;

        for (EventObserver<?> observer : getObserversForEvent(event)) {
            if (dispatchersToListeners == null) {
                dispatchersToListeners = new HashMap<>();
            }

            //noinspection unchecked
            Consumer<Object> listener = ((EventObserver<Object>) observer).getListener();
            EventDispatcher dispatcher = observer.getDispatcher();
            dispatchersToListeners.computeIfAbsent(dispatcher, k -> new ArrayList<>())
                    .add(listener);
        }

        if (dispatchersToListeners != null) {
            for (Map.Entry<EventDispatcher, List<Consumer<Object>>> entry : dispatchersToListeners.entrySet()) {
                entry.getKey().process(event, entry.getValue());
            }
        }
    }

    //endregion
    //region Observing Events
    private final SimpleSet<EventObserver<?>> allEventObservers = SimpleSet.newSimpleSet();

    @Override
    public <T> EventObserver<T> addObserver(EventObserver<T> eventObserver) {
        checkNotClosed();

        synchronized (allEventObservers) {
            if (!allEventObservers.add(eventObserver)) {
                throw new IllegalArgumentException(
                        "EventObserver was already added before.");
            }
            return eventObserver;
        }
    }

    public <T> EventObserver<T> addObserver(
            Class<T> eventType,
            @Nullable Object source,
            Predicate<T> condition,
            EventDispatcher dispatcher,
            Consumer<T> listener) {
        return addObserver(newEventObserverDefault(eventType, source, condition, dispatcher, listener));
    }

    @Override
    public void removeObserver(EventObserver<?> observer) {
        checkNotClosed();

        synchronized (allEventObservers) {
            allEventObservers.remove(observer);
        }
    }

    @Override
    public void removeAllObservers(Collection<EventObserver<?>> observers) {
        checkNotClosed();

        synchronized (allEventObservers) {
            allEventObservers.removeAll(observers);
        }
    }

    @Override
    public void removeAllObservers() {
        checkNotClosed();

        synchronized (allEventObservers) {
            allEventObservers.removeAll();
        }
    }

    @Override
    public void unobserveSource(Object sourceObject) {
        checkNotClosed();

        synchronized (allEventObservers) {
            allEventObservers.removeIf(e -> Objects.equals(e.getSource(), sourceObject));
        }
    }

    @Override
    public void unobserveAllSources(Collection<Object> sourceObjects) {
        checkNotClosed();

        synchronized (allEventObservers) {
            allEventObservers.removeIf(e -> sourceObjects.contains(e.getSource()));
        }
    }

    private Iterable<EventObserver<?>> getObserversForEvent(Object event) {
        checkNotClosed();

        synchronized (allEventObservers) {
            return allEventObservers.filtered(o -> isEventForObserver(event, o));
        }
    }

    private boolean isEventForObserver(Object event, EventObserver<?> observer) {
        // check event type
        if (!observer.getEventType().isAssignableFrom(event.getClass()))
            return false;

        // check source (of required)
        @Nullable Object requiredSource = observer.getSource();
        if (requiredSource != null
                && (!(event instanceof EventWithSource)
                || !requiredSource.equals(((EventWithSource) event).getSource()))) {
            return false;
        }

        // check condition
        //noinspection unchecked
        return ((EventObserver<Object>) observer).getCondition().test(event);
    }

    //endregion
    //region Dispatching Events
    private final AsyncDispatcherGroup asyncDispatcherGroup = newAsyncEventDispatcherGroupDefault(this);
    private final EventDispatcher asyncDispatcher = asyncDispatcherGroup.newAsyncDispatcher();

    @Override
    public EventDispatcher getDirectDispatcher() {
        checkNotClosed();

        return DirectDispatcher.getDefault();
    }

    @Override
    public ExplicitDispatcher newExplicitDispatcher(Consumer<ExplicitDispatcher> onDispatchPending) {
        checkNotClosed();

        return newExplicitDispatcherImpl(onDispatchPending);
    }

    @Override
    public EventDispatcher getAsyncDispatcher() {
        checkNotClosed();

        return asyncDispatcher;
    }

    @Override
    public AsyncDispatcherGroup newAsyncEventDispatcherGroup() {
        checkNotClosed();

        return newAsyncEventDispatcherGroupDefault(this);
    }

    //endregion
    //region Closing EventService
    private volatile boolean closed = false;

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() {
        closed = true;
        synchronized (allEventObservers) {
            allEventObservers.removeAll();
        }
        asyncDispatcherGroup.close();
    }

    private void checkNotClosed() {
        if (isClosed()) {
            throw new IllegalStateException(String.format(
                    "EventService already closed. %s", this));
        }
    }

    //endregion
}
