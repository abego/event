package org.abego.event;

import org.abego.commons.var.Var;
import org.abego.commons.var.VarUtil;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventObserverTest {
    @Test
    void smoketest() {
        Var<String> out = VarUtil.newVar();

        EventObserver<String> config = EventObserverDefault.newEventObserverDefault(
                String.class, null, o->true, (event, listeners) -> {
                    // do nothing
                }, out::set);

        assertEquals(String.class, config.getEventType());
        config.getListener().accept("foo");
        assertEquals("foo", out.get());
        assertTrue(config.getCondition().test("bar"));
        assertNull(config.getSource());
        assertNotNull(config.getDispatcher());
    }

    @Test
    void sourceAndConditionDefaults() {
        EventObserver<String> eo = new EventObserver<String>(){
            @Override
            public Class<String> getEventType() {
                return String.class;
            }

            @Override
            public EventDispatcher getDispatcher() {
                return EventServices.getDefault().getDefaultDispatcher();
            }

            @Override
            public Consumer<String> getListener() {
                return s->{};
            }
        };

        assertNull(eo.getSource());
        assertTrue(eo.getCondition().test("foo"));
        assertTrue(eo.getCondition().test("bar"));
    }
}