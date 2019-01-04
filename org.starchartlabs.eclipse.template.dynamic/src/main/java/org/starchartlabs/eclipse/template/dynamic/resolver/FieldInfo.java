package org.starchartlabs.eclipse.template.dynamic.resolver;

import java.util.Optional;

/**
 * Represents data associated with a given field
 *
 * @author desprez, romeara
 */
public class FieldInfo {

    private final String type;

    private final String getter;

    private final Optional<String> setter;

    public FieldInfo(String type, String getter, String setter) {
        this.type = type;
        this.getter = getter;
        this.setter = Optional.ofNullable(setter);
    }

    public String getType() {
        return type;
    }

    public String getGetter() {
        return getter;
    }

    public Optional<String> getSetter() {
        return setter;
    }

}
