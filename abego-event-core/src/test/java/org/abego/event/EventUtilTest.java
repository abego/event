package org.abego.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class EventUtilTest {
    @Test
    void constructor() {
        assertThrows(UnsupportedOperationException.class, EventUtil::new);
    }
}
