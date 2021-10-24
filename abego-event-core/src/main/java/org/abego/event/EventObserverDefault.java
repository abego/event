package org.abego.event;

import org.eclipse.jdt.annotation.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;

class EventObserverDefault<T> implements EventObserver<T> {
    private final Configuration<T> configuration;

    private EventObserverDefault(Configuration<T> configuration) {
        this.configuration = configuration;
    }

    public static <T> EventObserverDefault<T> newEventObserverDefault(Configuration<T> configuration) {
        return new EventObserverDefault<>(configuration);
    }

    @Override
    public Class<T> getEventType() {
        return configuration.getEventType();
    }

    @Override
    public Consumer<T> getListener() {
        return configuration.getListener();
    }

    @Override
    public Predicate<T> getCondition() {
        return configuration.getCondition();
    }

    @Override
    public @Nullable Object getSource() {
        return configuration.getSource();
    }

    @Override
    public EventDispatcher getDispatcher() {
        return configuration.getDispatcher();
    }
}
