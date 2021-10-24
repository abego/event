package org.abego.event;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.abego.commons.seq.SeqUtil.newSeq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventServiceDefaultTest {

    @Test
    void getDefault_alwaysSameInstance() {
        EventService service1 = EventServiceDefault.getDefault();
        EventService service2 = EventServiceDefault.getDefault();

        assertSame(service1, service2);
    }

    @Test
    void newInstance_notSameInstance() {
        EventService service1 = EventServiceDefault.newInstance();
        EventService service2 = EventServiceDefault.newInstance();

        assertNotSame(service1, service2);
    }

    @Test
    void getDirectDispatcher() {
        EventService service = EventServiceDefault.getDefault();

        EventDispatcher dispatcher = service.getDirectDispatcher();

        assertNotNull(dispatcher);
    }

    @Test
    void standard_workflow() {
        List<Object> log = new ArrayList<>();
        EventService service = EventServiceDefault.newInstance();

        service.addObserver(String.class,
                o -> log.add(String.format("String: %s", o)));
        service.addObserver(Integer.class,
                o -> log.add(String.format("Integer: %s", o)));
        service.addObserver(Object.class,
                o -> log.add(String.format("Object: %s", o)));

        service.postEvent("foo");
        service.postEvent(123);

        assertEquals("" +
                        "Integer: 123\n" +
                        "Object: 123\n" +
                        "Object: foo\n" +
                        "String: foo",
                newSeq(log).sorted().joined("\n"));
    }

    @Test
    void standard_workflow_withPredicate() {
        List<Object> log = new ArrayList<>();
        EventService service = EventServiceDefault.newInstance();

        EventObserver<String> o1 = service.addObserver(String.class, s -> s.length() > 3,
                o -> log.add(String.format("size > 3: %s", o)));
        EventObserver<String> o2 = service.addObserver(String.class, s -> true,
                o -> log.add(String.format("any size: %s", o)));
        EventObserver<String> o3 = service.addObserver(String.class, s -> s.length() % 2 == 1,
                service.getDirectDispatcher(), // explictly pass in the "default" dispatcher
                o -> log.add(String.format("odd size: %s", o)));

        service.postEvent("foo");
        service.postEvent("foobar");
        service.postEvent("foobarbaz");

        assertEquals("" +
                        "any size: foo\n" +
                        "any size: foobar\n" +
                        "any size: foobarbaz\n" +
                        "odd size: foo\n" +
                        "odd size: foobarbaz\n" +
                        "size > 3: foobar\n" +
                        "size > 3: foobarbaz",
                newSeq(log).sorted().joined("\n"));

        assertNotNull(o1);// to avoid "unused return value" warnings
        assertNotNull(o2);// to avoid "unused return value" warnings
        assertNotNull(o3);// to avoid "unused return value" warnings
    }


    @Test
    void standard_workflow_withSource() {
        List<Object> log = new ArrayList<>();
        EventService service = EventServiceDefault.newInstance();

        EventObserver<EventWithSource> o1 = service.addObserver(EventWithSource.class, "foo",
                o -> log.add(String.format("fooObserver: %s", o)));
        service.addObserver(EventWithSource.class, "bar",
                o -> log.add(String.format("barObserver: %s", o)));
        service.addObserver(EventWithSource.class,
                o -> log.add(String.format("ObjectObserver: %s", o)));

        service.postEvent(EventTestUtil.newEventWithSource("foo", "123"));
        service.postEvent(EventTestUtil.newEventWithSource("bar", "456"));

        assertEquals("" +
                        "ObjectObserver: 123\n" +
                        "ObjectObserver: 456\n" +
                        "barObserver: 456\n" +
                        "fooObserver: 123",
                newSeq(log).sorted().joined("\n"));
        assertNotNull(o1); // to avoid "unused return value warning
    }

    @Test
    void standard_workflow_withNullSourceObserved() {
        List<Object> log = new ArrayList<>();
        EventService service = EventServiceDefault.newInstance();

        EventObserver<EventWithSource> o1 = service.addObserver(
                EventWithSource.class, null, x -> true,
                service.getDirectDispatcher(),
                o -> log.add(String.format("allObserver: %s", o)));

        service.postEvent(EventTestUtil.newEventWithSource("foo", "123"));
        service.postEvent(EventTestUtil.newEventWithSource("bar", "456"));

        assertEquals("" +
                        "allObserver: 123\n" +
                        "allObserver: 456",
                newSeq(log).sorted().joined("\n"));
        assertNotNull(o1); // to avoid "unused return value warning
    }

    @Test
    void standard_workflow_withSourceAndPredicate() {
        List<Object> log = new ArrayList<>();
        EventService service = EventServiceDefault.newInstance();

        EventObserver<EventWithSource> o1 = service.addObserver(EventWithSource.class, "joe",
                s -> s.toString().length() > 3,
                o -> log.add(String.format("size > 3, from joe: %s", o)));
        service.addObserver(EventWithSource.class, "joe",
                s -> true,
                o -> log.add(String.format("any size, from joe: %s", o)));
        service.addObserver(EventWithSource.class, "joe",
                s -> s.toString().length() % 2 == 1,
                o -> log.add(String.format("odd size, from joe: %s", o)));

        service.postEvent(EventTestUtil.newEventWithSource("joe", "foo"));
        service.postEvent(EventTestUtil.newEventWithSource("jane", "foobar"));
        service.postEvent(EventTestUtil.newEventWithSource("joe", "foobarbaz"));

        assertEquals("" +
                        "any size, from joe: foo\n" +
                        "any size, from joe: foobarbaz\n" +
                        "odd size, from joe: foo\n" +
                        "odd size, from joe: foobarbaz\n" +
                        "size > 3, from joe: foobarbaz",
                newSeq(log).sorted().joined("\n"));
        assertNotNull(o1);// to avoid "unused return value" warnings
    }


    @Test
    void removeObserver() {
        List<Object> log = new ArrayList<>();
        EventService service = EventServiceDefault.newInstance();

        EventObserver<String> observer1 = service.addObserver(String.class,
                o -> log.add(String.format("observer1: %s", o)));
        EventObserver<String> observer2 = service.addObserver(String.class,
                o -> log.add(String.format("observer2: %s", o)));

        service.postEvent("foo");
        service.removeObserver(observer1);
        service.postEvent("bar");
        service.removeObserver(observer2);
        service.postEvent("baz");

        assertEquals("" +
                        "observer1: foo\n" +
                        "observer2: bar\n" +
                        "observer2: foo",
                newSeq(log).sorted().joined("\n"));
    }

    @Test
    void standard_workflow_ExplicitDispatcher() {
        List<Object> log = new ArrayList<>();
        EventService service = EventServiceDefault.newInstance();
        ExplicitDispatcher dispatcher = service.newExplicitDispatcher();

        EventObserver<String> observer1 = service.addObserver(String.class,
                dispatcher, o -> log.add(String.format("observer1: %s", o)));
        EventObserver<String> observer2 = service.addObserver(String.class,
                dispatcher, o -> log.add(String.format("observer2: %s", o)));

        service.postEvent("foo");
        service.removeObserver(observer1);
        service.postEvent("bar");
        service.removeObserver(observer2);
        service.postEvent("baz");

        // no items logged as the dispatcher items are not yet dispatched
        assertEquals("", newSeq(log).sorted().joined("\n"));

        dispatcher.dispatch();

        assertEquals("" +
                        "observer1: foo\n" +
                        "observer2: bar\n" +
                        "observer2: foo",
                newSeq(log).sorted().joined("\n"));
    }

    @Test
    void standard_workflow_ExplicitDispatcher_DispatchPendingCode() {
        List<Object> log = new ArrayList<>();
        EventService service = EventServiceDefault.newInstance();
        ExplicitDispatcher dispatcher = service.newExplicitDispatcher(
                e -> log.add("dispatchPending"));

        service.addObserver(String.class,
                dispatcher, o -> log.add(String.format("observer: %s", o)));
        log.add("will post 'foo'");
        service.postEvent("foo");
        log.add("did post 'foo'");
        log.add("will post 'bar'");
        service.postEvent("bar");
        log.add("did post 'bar'");

        log.add("will dispatch");
        dispatcher.dispatch();
        log.add("did dispatch");

        log.add("will post 'baz'");
        service.postEvent("baz");
        log.add("did post 'baz'");

        log.add("will dispatch");
        dispatcher.dispatch();
        log.add("did dispatch");

        assertEquals("" +
                        "will post 'foo'\n" +
                        "dispatchPending\n" +
                        "did post 'foo'\n" +
                        "will post 'bar'\n" +
                        "did post 'bar'\n" +
                        "will dispatch\n" +
                        "observer: foo\n" +
                        "observer: bar\n" +
                        "did dispatch\n" +
                        "will post 'baz'\n" +
                        "dispatchPending\n" +
                        "did post 'baz'\n" +
                        "will dispatch\n" +
                        "observer: baz\n" +
                        "did dispatch",
                newSeq(log).joined("\n"));
    }

    @Test
    void standard_workflow_ExplicitDispatcher_withSource() {
        List<Object> log = new ArrayList<>();
        EventService service = EventServiceDefault.newInstance();
        ExplicitDispatcher dispatcher = service.newExplicitDispatcher();

        EventObserver<EventWithSource> o1 = service.addObserver(EventWithSource.class, "foo", dispatcher,
                o -> log.add(String.format("fooObserver: %s", o)));
        service.addObserver(EventWithSource.class, "bar", dispatcher,
                o -> log.add(String.format("barObserver: %s", o)));
        service.addObserver(EventWithSource.class, dispatcher,
                o -> log.add(String.format("ObjectObserver: %s", o)));

        assertFalse(dispatcher.isDispatchPending());

        service.postEvent(EventTestUtil.newEventWithSource("foo", "123"));
        service.postEvent(EventTestUtil.newEventWithSource("bar", "456"));

        assertTrue(dispatcher.isDispatchPending());
        // not yet dispatched
        assertEquals("", newSeq(log).sorted().joined("\n"));

        dispatcher.dispatch();

        assertFalse(dispatcher.isDispatchPending());

        assertEquals("" +
                        "ObjectObserver: 123\n" +
                        "ObjectObserver: 456\n" +
                        "barObserver: 456\n" +
                        "fooObserver: 123",
                newSeq(log).sorted().joined("\n"));
        assertNotNull(o1);// to avoid "unused return value" warnings
    }

    @Test
    void newAsyncEventDispatcherGroup() {
        EventService service = EventServiceDefault.newInstance();
        AsyncDispatcherGroup group = service.newAsyncEventDispatcherGroup();

        assertNotNull(group);
    }

    @Test
    void getAsyncDispatcher() {
        EventService service = EventServiceDefault.newInstance();
        EventDispatcher dispatcher1 = service.getAsyncDispatcher();
        EventDispatcher dispatcher2 = service.getAsyncDispatcher();

        assertNotNull(dispatcher1);
        assertSame(dispatcher1, dispatcher2);
    }

    @Test
    void close() {
        EventService service = EventServiceDefault.newInstance();
        EventObserver<Object> toBeRemoved =
                service.addObserver(Object.class, o -> {});

        assertFalse(service.isClosed());

        service.close();

        assertTrue(service.isClosed());
        assertThrows(IllegalStateException.class, () -> service.addObserver(Object.class, o -> {}));
        assertThrows(IllegalStateException.class, service::getAsyncDispatcher);
        assertThrows(IllegalStateException.class, service::getDirectDispatcher);
        assertThrows(IllegalStateException.class, service::newAsyncEventDispatcherGroup);
        assertThrows(IllegalStateException.class, service::newExplicitDispatcher);
        assertThrows(IllegalStateException.class, () -> service.postEvent(""));
        assertThrows(IllegalStateException.class, () -> service.removeObserver(toBeRemoved));
    }

    @Test
    void properyChanged() {
        List<Object> log = new ArrayList<>();
        EventService service = EventServiceDefault.newInstance();

        Object o1 = new Object() {
            @Override
            public String toString() {
                return "o1";
            }
        };
        Object o2 = new Object() {
            @Override
            public String toString() {
                return "o2";
            }
        };
        service.addPropertyObserver(o1, "foo", e -> log.add("o1.foo changed"));
        service.addPropertyObserver(o1, "bar", e -> log.add("o1.bar changed"));
        service.addPropertyObserver(o1, e -> log.add(String.format(
                "o1 property changed. %s", e)));
        service.addPropertyObserver(o1, e-> e.getDetails() != null ,e -> log.add(String.format(
                "o1 property with details changed. %s", e)));
        service.addPropertyObserver(o2, e -> log.add(String.format(
                "o2 property changed. %s", e)));
        service.addPropertyObserver(o2, "foo", service.getDirectDispatcher(), e -> log.add(String.format(
                "o2.foo changed. %s", e)));
        service.addPropertyObserver(o2, e-> e.getDetails() != null ,service.getDirectDispatcher(), e -> log.add(String.format(
                "o2 property with details changed. %s", e)));

        service.postPropertyChanged(o1, "foo");
        service.postPropertyChanged(o1, "bar", "[1]");
        service.postPropertyChanged(o1, "baz", "+[3-5]");
        service.postPropertyChanged(o2, "foo");
        service.postPropertyChanged(o2, "bar", "[2]");

        // as the order of observations may change we sort the log for comparision
        assertEquals("" +
                        "o1 property changed. PropertyChangedDefault{propertyName='bar', source=o1, details=[1]}\n" +
                        "o1 property changed. PropertyChangedDefault{propertyName='baz', source=o1, details=+[3-5]}\n" +
                        "o1 property changed. PropertyChangedDefault{propertyName='foo', source=o1, details=null}\n" +
                        "o1 property with details changed. PropertyChangedDefault{propertyName='bar', source=o1, details=[1]}\n" +
                        "o1 property with details changed. PropertyChangedDefault{propertyName='baz', source=o1, details=+[3-5]}\n" +
                        "o1.bar changed\n" +
                        "o1.foo changed\n" +
                        "o2 property changed. PropertyChangedDefault{propertyName='bar', source=o2, details=[2]}\n" +
                        "o2 property changed. PropertyChangedDefault{propertyName='foo', source=o2, details=null}\n" +
                        "o2 property with details changed. PropertyChangedDefault{propertyName='bar', source=o2, details=[2]}\n" +
                        "o2.foo changed. PropertyChangedDefault{propertyName='foo', source=o2, details=null}",
                newSeq(log).sorted().joined("\n"));

    }
}