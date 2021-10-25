package org.abego.event;

import org.abego.commons.var.Var;
import org.abego.commons.var.VarUtil;
import org.junit.jupiter.api.Test;

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

}