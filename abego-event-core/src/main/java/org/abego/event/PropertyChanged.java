package org.abego.event;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Describes a change to a property of the object returned by
 * {@link EventWithSource#getSource()}.
 */
public interface PropertyChanged extends EventWithSource {
    /**
     * Returns the name of the property of {@link PropertyChanged#getSource()}
     * that changed.
     */
    String getName();

    /**
     * Returns details of the change, or {@code null} when there are no details.
     *
     * <p>The type of the details (if they exist) are specific to the change,
     */
    @Nullable Object getDetails();
}
