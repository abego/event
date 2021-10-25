package org.abego.event;

import org.eclipse.jdt.annotation.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface EventObserver<T> {

    Class<T> getEventType();

    Consumer<T> getListener();

    default Predicate<T> getCondition() {
        return o -> true;
    }

    default @Nullable Object getSource() {
        return null;
    }

    EventDispatcher getDispatcher();
}
