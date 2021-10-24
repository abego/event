package org.abego.event;

import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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


class EventServiceDefault implements EventService {
    private static final EventService DEFAULT_INSTANCE = newInstance();
    private final SimpleSet<EventObserver<?>> allEventObservers = SimpleSet.newSimpleSet();
    private final AsyncDispatcherGroup asyncDispatcherGroup = newAsyncEventDispatcherGroupDefault(this);
    private final EventDispatcher asyncDispatcher = asyncDispatcherGroup.newAsyncDispatcher();

    private boolean closed = false;

    private EventServiceDefault() {
    }

    public static EventService getDefault() {
        return DEFAULT_INSTANCE;
    }

    public static EventService newInstance() {
        return new EventServiceDefault();
    }

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

    @Override
    public <T> EventObserver<T> addObserver(EventObserver.Configuration<T> eventObserverConfiguration) {
        checkNotClosed();

        EventObserverDefault<T> observer = newEventObserverDefault(eventObserverConfiguration);
        allEventObservers.add(observer);
        return observer;
    }

    @Override
    public void removeObserver(EventObserver<?> observer) {
        checkNotClosed();

        allEventObservers.remove(observer);
    }

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

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() {
        closed = true;
        asyncDispatcherGroup.close();
    }

    private void checkNotClosed() {
        if (isClosed()) {
            throw new IllegalStateException(String.format(
                    "EventService already closed. %s", this));
        }
    }

    private Iterable<EventObserver<?>> getObserversForEvent(Object event) {
        return allEventObservers.filtered(o -> isEventForObserver(event, o));
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

}
