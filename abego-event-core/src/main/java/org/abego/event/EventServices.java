package org.abego.event;

public final class EventServices {
    EventServices() {
        throw new IllegalArgumentException("Must not instantiate");
    }

    public static EventService getDefault() {
        return EventServiceDefault.getDefault();
    }

    public static EventService newEventService() {
        return EventServiceDefault.newInstance();
    }

}
