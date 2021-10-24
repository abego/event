package org.abego.event;

import org.eclipse.jdt.annotation.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface EventObserver<T> {
    interface Configuration<T> {

        Class<T> getEventType();

        Consumer<T> getListener();

        EventDispatcher getDispatcher();

        default Predicate<T> getCondition() {
            return o -> true;
        }

        default @Nullable Object getSource() {
            return null;
        }

    }

    Class<T> getEventType();

    Consumer<T> getListener();

    Predicate<T> getCondition();

    @Nullable Object getSource();

    EventDispatcher getDispatcher();
}
