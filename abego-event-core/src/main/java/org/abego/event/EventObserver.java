package org.abego.event;

import org.eclipse.jdt.annotation.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.abego.commons.util.function.PredicateUtil.alwaysTrue;

public interface EventObserver<T> {
    interface Configuration<T> {

        Class<T> getEventType();

        Consumer<T> getListener();

        EventDispatcher getDispatcher();

        default Predicate<T> getCondition() {
            return alwaysTrue();
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
