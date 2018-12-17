package org.starchartlabs.eclipse.template.dynamic.resolver;

public class FieldInfo {

    private String getter;

    private String setter;

    private String type;

    public String getGetter() {
        return getter;
    }

    public void setGetter(final String getter) {
        this.getter = getter;
    }

    public String getSetter() {
        return setter;
    }

    public void setSetter(final String setter) {
        this.setter = setter;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

}
