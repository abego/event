package org.abego.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventServicesTest {
    @Test
    void constructor() {
        assertThrows(UnsupportedOperationException.class, EventServices::new);
    }

    @Test
    void getDefault() {
        EventService service = EventServices.getDefault();
        EventService service2 = EventServices.getDefault();

        assertSame(service, service2);
    }

    @Test
    void newEventService() {
        EventService service = EventServices.newEventService();
        EventService service2 = EventServices.newEventService();

        assertNotSame(service, service2);
    }
}