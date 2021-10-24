package org.abego.event;

import org.abego.commons.lang.exception.MustNotInstantiateException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class EventUtilTest {
    @Test
    void constructor() {
        assertThrows(MustNotInstantiateException.class,EventUtil::new);
    }
}
