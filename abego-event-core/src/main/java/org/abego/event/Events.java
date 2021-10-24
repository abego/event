package org.abego.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * List of types used as Events posted by the annotated entity.
 *
 * <p>When the event is of type {@link EventWithSource} the source is typically
 * an instance of the annotated class (or class of the annotated methods).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Events {
    Class<?>[] value();
}
