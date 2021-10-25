package org.abego.event;

import org.eclipse.jdt.annotation.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface EventService extends AutoCloseable {

    /**
     * Post the given event object to the {@link EventService}.
     *
     * @param event the event to be posted. Can be of any type, however its
     *              type selects the observers being notified. See also
     *              {@link EventWithSource}.
     */
    void postEvent(Object event);

    /**
     * Post a {@link PropertyChanged} event to the {@link EventService}.
     *
     * @param source       the object that property changed.
     * @param propertyName the name of the property that changed.
     * @param details      details of the change,
     *                     or {@code null} when there are no details.
     */
    default void postPropertyChanged(
            Object source, String propertyName, @Nullable Object details) {
        postEvent(PropertyChangedDefault.newPropertyChangedDefault(
                propertyName, source, details));
    }

    /**
     * Post a {@link PropertyChanged} event to the {@link EventService}.
     * <p>
     * As {@link #postPropertyChanged(Object, String, Object)}, but with no
     * details.
     *
     * @param source       the object that property changed.
     * @param propertyName the name of the property that changed.
     */
    default void postPropertyChanged(Object source, String propertyName) {
        postEvent(PropertyChangedDefault.newPropertyChangedDefault(
                propertyName, source, null));
    }


    /**
     * Adds the  {@code eventObserver} to the {@link EventService}
     * and returns it.
     * <p>
     * The operation will fail when the {@link EventService} already contains
     * the {@code eventObserver}.
     *
     * @param eventObserver The {@link EventObserver} to add
     * @param <T>           The type of events to observe
     * @return The {@link EventObserver} ({@code eventObserver}
     */
    <T> EventObserver<T> addObserver(EventObserver<T> eventObserver);

    /**
     * Creates a new {@link EventObserver} based on the given parameters,
     * adds it to the {@link EventService} and returns the new EventObserver.
     *
     * @param eventType  The type of events to observe
     * @param source     When not {@code null} only events with this source are
     *                   observed.
     * @param condition  Only events matching the condition are observed.
     * @param dispatcher The {@link EventDispatcher} serving the EventObserver
     * @param listener   The code to run when the observer receives an event
     * @param <T>        The type of events to observe
     * @return the newly created {@link EventObserver}
     */
    <T> EventObserver<T> addObserver(
            Class<T> eventType,
            @Nullable Object source,
            Predicate<T> condition,
            EventDispatcher dispatcher,
            Consumer<T> listener);

    default <T> EventObserver<T> addObserver(
            Class<T> eventType,
            Consumer<T> listener) {
        return addObserver(
                eventType, null, o -> true, getDefaultDispatcher(), listener);
    }

    default <T> EventObserver<T> addObserver(
            Class<T> eventType,
            Predicate<T> condition,
            EventDispatcher dispatcher,
            Consumer<T> listener) {
        return addObserver(
                eventType, null, condition, dispatcher, listener);
    }

    default <T> EventObserver<T> addObserver(
            Class<T> eventType,
            Predicate<T> condition,
            Consumer<T> listener) {
        return addObserver(
                eventType, null, condition, getDefaultDispatcher(), listener);
    }

    default <T> EventObserver<T> addObserver(
            Class<T> eventType,
            EventDispatcher dispatcher,
            Consumer<T> listener) {
        return addObserver(
                eventType, null, o -> true, dispatcher, listener);
    }

    default <T> EventObserver<T> addObserver(
            Class<T> eventType,
            @Nullable Object source,
            Consumer<T> listener) {
        return addObserver(
                eventType, source, o -> true, getDefaultDispatcher(), listener);
    }

    default <T> EventObserver<T> addObserver(
            Class<T> eventType,
            @Nullable Object source,
            Predicate<T> condition,
            Consumer<T> listener) {
        return addObserver(
                eventType, source, condition, getDefaultDispatcher(), listener);
    }

    default <T> EventObserver<T> addObserver(
            Class<T> eventType,
            @Nullable Object source,
            EventDispatcher dispatcher,
            Consumer<T> listener) {
        return addObserver(eventType, source, o -> true, dispatcher, listener);
    }

    default EventObserver<PropertyChanged> addPropertyObserver(
            Object source,
            @Nullable String propertyName,
            Predicate<PropertyChanged> condition,
            EventDispatcher dispatcher,
            Consumer<PropertyChanged> listener) {
        return addObserver(PropertyChanged.class,
                source,
                e -> (propertyName == null ||
                        e.getName().equals(propertyName)) && condition.test(e),
                dispatcher,
                listener);
    }

    default EventObserver<PropertyChanged> addPropertyObserver(
            Object source,
            // any property name,
            Predicate<PropertyChanged> condition,
            EventDispatcher dispatcher,
            Consumer<PropertyChanged> listener) {
        return addPropertyObserver(
                source, null, condition, dispatcher, listener);
    }

    default EventObserver<PropertyChanged> addPropertyObserver(
            Object source,
            // any property name,
            Predicate<PropertyChanged> condition,
            // use defaultDispatcher,
            Consumer<PropertyChanged> listener) {
        return addPropertyObserver(
                source, null, condition, getDefaultDispatcher(), listener);
    }

    default EventObserver<PropertyChanged> addPropertyObserver(
            Object source,
            @Nullable String propertyName,
            // no extra condition (just the property name),
            EventDispatcher dispatcher,
            Consumer<PropertyChanged> listener) {
        return addPropertyObserver(
                source, propertyName, o -> true, dispatcher, listener);
    }

    default EventObserver<PropertyChanged> addPropertyObserver(
            Object source,
            @Nullable String propertyName,
            // no extra condition (just the property name),
            // use defaultDispatcher,
            Consumer<PropertyChanged> listener) {
        return addPropertyObserver(
                source, propertyName, o -> true, getDefaultDispatcher(), listener);
    }

    default EventObserver<PropertyChanged> addPropertyObserver(
            Object source,
            // any property name,
            // no extra condition (just the property name),
            // use defaultDispatcher,
            Consumer<PropertyChanged> listener) {
        return addPropertyObserver(
                source, null, o -> true, getDefaultDispatcher(), listener);
    }

    void removeObserver(EventObserver<?> observer);

    default EventDispatcher getDefaultDispatcher() {
        return getDirectDispatcher();
    }

    EventDispatcher getDirectDispatcher();

    ExplicitDispatcher newExplicitDispatcher(
            Consumer<ExplicitDispatcher> onDispatchPending);

    default ExplicitDispatcher newExplicitDispatcher() {
        return newExplicitDispatcher(e -> {});
    }

    EventDispatcher getAsyncDispatcher();

    AsyncDispatcherGroup newAsyncEventDispatcherGroup();

    void close();

    boolean isClosed();
}
