package org.abego.event;

import org.eclipse.jdt.annotation.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface EventObserver<T> {

    Class<T> getEventType();

    default @Nullable Object getSource() {
        return null;
    }

    default Predicate<T> getCondition() {
        return o -> true;
    }

    EventDispatcher getDispatcher();

    Consumer<T> getListener();
}
