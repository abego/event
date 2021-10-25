package org.abego.event;

import org.eclipse.jdt.annotation.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;

final class EventObserverDefault<T> implements EventObserver<T> {
    private final Class<T> eventType;
    private final @Nullable Object source;
    private final Predicate<T> condition;
    private final EventDispatcher dispatcher;
    private final Consumer<T> listener;

    private EventObserverDefault(
            Class<T> eventType,
            @Nullable Object source,
            Predicate<T> condition,
            EventDispatcher dispatcher,
            Consumer<T> listener) {

        this.eventType = eventType;
        this.listener = listener;
        this.condition = condition;
        this.source = source;
        this.dispatcher = dispatcher;
    }

    public static <T> EventObserverDefault<T> newEventObserverDefault(
            Class<T> eventType,
            @Nullable Object source,
            Predicate<T> condition,
            EventDispatcher dispatcher,
            Consumer<T> listener) {

        return new EventObserverDefault<>(eventType, source, condition, dispatcher, listener);
    }

    @Override
    public Class<T> getEventType() {
        return eventType;
    }

    @Override
    public Consumer<T> getListener() {
        return listener;
    }

    @Override
    public Predicate<T> getCondition() {
        return condition;
    }

    @Override
    public @Nullable Object getSource() {
        return source;
    }

    @Override
    public EventDispatcher getDispatcher() {
        return dispatcher;
    }
}
