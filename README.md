# Eclipse Java Code Template Dynamic Variables Plug-in

Eclipse Plug-in to allow templates actions based on Java Bean fields present in a target class

See recent activity in the [Change Log](CHANGELOG.md)

# Installation

The Template Dynamic Variables plug-in is hosted via Eclipse update site at the address:

`https://raw.githubusercontent.com/StarChart-Labs/eclipse-dynamic-templates/master/org.starchartlabs.eclipse.template.dynamic.site/`

# Use

This plug-in added a variable to the available variables for use in Java code templates. The field `enclosed_bean_fields` may be used to subsitute code per occurance of a field with a matching-named getter within the class being edited. A matching-name getter has no parameters, and is named `get(field)`, with the field's first letter capitalized. Boolean fields also match against `is(field)`.

The variable is used in the form

`${id:enclosed_bean_fields(template, separator)}`

The creator of the template replaces the following in the above:

- `id`
  - An identifier and default value (unique within the template) to use if there is a resolution error
- `template`
  - Code to subsitute per field/getter set within the enclosing class. Users may use the value `${name}` and `$getter}` to substitute the field name or corresponding getter name of of the field/getter pair
- `separator`
  - Code to place between each occurance of the template generated. Users may use the value `${newline}` to substitute a newline into the separator. If code formatting is enabled for templates, its application may override the resulting newlines
  
## Example

The following template uses the `enclosed_bean_fields` variable:

```
public String toString(){
  return getClass().getSimpleName() + "{"
    + ${strfields:enclosed_bean_fields('"${name}=" + ${getter}', ' + "," ${newline}+ ')}
    "}";
}
```

If used within the class:

```
public class Example {

  private String field;
  
  private boolean boolField;
  
  private String noMatchedGetter;
  
  public String getField(){
    return field;
  }
  
  public boolean isBoolField(){
    return boolField;
  }
  
}
```

The template would result in the code:

```
public String toString(){
  return getClass().getSimpleName() + "{"
    + "field=" + getField() + ","
    + "boolField=" + isBoolField()
    "}";
}
```

# Contribution

Contributions are always welcome! 

* An Eclipse plug-in development environment is required to work with the projects - the last used Eclipse version was Oxygen. 
* The plug-ins are currently developed against Java 8
* All pull requests should be done against the master branch

## Licensing

The repository working set plug in is licensed under the Eclipse Public License 1.0, as specified and documented in the LICENSE.md file within each plug-in

