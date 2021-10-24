package org.abego.event;

import org.abego.commons.lang.exception.MustNotInstantiateException;

import java.util.function.Consumer;
import java.util.function.Supplier;

final class EventUtil {
    EventUtil() {
        throw new MustNotInstantiateException();
    }


    static Supplier<String> getDispatchErrorMessageSupplier(
            Object event, Consumer<Object> listener, Exception e) {
        return () ->
                String.format("Error when dispatching event `%s` to `%s`: %s",
                        event, listener, e.getMessage());
    }
}
