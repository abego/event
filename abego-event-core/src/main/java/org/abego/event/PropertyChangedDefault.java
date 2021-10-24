package org.abego.event;

import org.eclipse.jdt.annotation.Nullable;

class PropertyChangedDefault implements PropertyChanged {
    private final String propertyName;
    private final Object source;
    @Nullable
    private final Object details;

    private PropertyChangedDefault(String propertyName, Object source, @Nullable Object details) {
        this.propertyName = propertyName;
        this.source = source;
        this.details = details;
    }

    public static PropertyChangedDefault newPropertyChangedDefault(
            String propertyName, Object source, @Nullable Object details) {
        return new PropertyChangedDefault(propertyName, source, details);
    }

    @Override
    public String getName() {
        return propertyName;
    }

    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public @Nullable Object getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return "PropertyChangedDefault{" +
                "propertyName='" + propertyName + '\'' +
                ", source=" + source +
                ", details=" + details +
                '}';
    }
}
