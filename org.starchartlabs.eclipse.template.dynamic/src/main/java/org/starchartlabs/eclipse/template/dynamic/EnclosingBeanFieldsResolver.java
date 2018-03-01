/*
 * Copyright (c) Feb 28, 2018 StarChart Labs Authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    romeara - initial API and implementation and/or initial documentation
 */
package org.starchartlabs.eclipse.template.dynamic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.template.java.CompilationUnitContext;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

/**
 * Fields resolver which allows defining a template to fill for every Java "bean" field
 *
 * <p>
 * In this context, a "bean" field is defined as a field which has a matching method called "get(Fieldname)". For
 * booleans, "is(Fieldname)" will also be considered a match
 *
 * <p>
 * References:
 * <ul>
 * <li>org.eclipse.jface.text.templates.TemplateVariable</li>
 * <li>org.eclipse.jdt.internal.corext.template.java.CompilationUnitContextType</li>
 * <li>org.eclipse.jdt.internal.corext.template.java.JavaContextType</li>
 * <li>https://stackoverflow.com/questions/350600/eclipse-custom-variable-for-java-code-templates</li>
 * <li>https://help.eclipse.org/mars/index.jsp?topic=%2Forg.eclipse.platform.doc.isv%2Freference%2Fapi%2Forg%2Feclipse%2Fjface%2Ftext%2Ftemplates%2FTemplateVariableResolver.html</li>
 * <li>http://help.eclipse.org/kepler/index.jsp?topic=%2Forg.eclipse.jdt.doc.user%2Fconcepts%2Fconcept-template-variables.htm</li>
 * </ul>
 *
 * @author romeara
 */
@SuppressWarnings("restriction")
public class EnclosingBeanFieldsResolver extends TemplateVariableResolver {

    private static final String NAME_PLACEHOLDER = "\\$\\{name\\}";

    private static final String GETTER_PLACEHOLDER = "\\$\\{getter\\}";

    private static final Set<String> BOOLEAN_TYPE_SIGNATURES = Stream.of("Z", "QBoolean;").collect(Collectors.toSet());

    @Override
    public void resolve(TemplateVariable variable, TemplateContext context) {
        if (context instanceof CompilationUnitContext) {
            CompilationUnitContext jc = (CompilationUnitContext) context;

            String[] bindings = resolveAll(jc, variable.getVariableType().getParams());

            if (bindings != null) {
                variable.setValues(bindings);
                variable.setUnambiguous(true);
                variable.setResolved(true);
            } else {
                super.resolve(variable, context);
            }
        } else {
            super.resolve(variable, context);
        }
    }

    protected String[] resolveAll(CompilationUnitContext context, List<String> params) {
        String[] result = null;

        if (params.size() == 3) {
            String template = params.get(0);
            String separator = params.get(1);
            boolean newline = Boolean.valueOf(params.get(2));

            separator = (newline ? "\n" + separator : separator);

            List<String> lines = new ArrayList<>();

            for (Entry<String, String> entry : getBeanPairs(context).entrySet()) {
                lines.add(template
                        .replaceAll(NAME_PLACEHOLDER, entry.getKey())
                        .replaceAll(GETTER_PLACEHOLDER, entry.getValue()));
            }

            String value = lines.stream().collect(Collectors.joining(separator));
            result = new String[] { value };
        }

        return result;
    }

    protected Map<String, String> getBeanPairs(CompilationUnitContext context) {
        Map<String, String> result = new LinkedHashMap<String, String>();

        try {
            IType type = (IType) context.findEnclosingElement(IJavaElement.TYPE);
            Set<String> methodLookup = getCandidateMethods(type);

            for (IField field : type.getFields()) {
                String capitializedName = capitializeFirstLetter(field.getElementName());
                String getter = "get" + capitializedName;
                String isGetter = "is" + capitializedName;

                if (methodLookup.contains(getter)) {
                    result.put(field.getElementName(), getter + "()");
                } else if (isBooleanField(field) && methodLookup.contains(isGetter)) {
                    result.put(field.getElementName(), isGetter + "()");
                }
            }
        } catch (JavaModelException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    private Set<String> getCandidateMethods(IType type) throws JavaModelException {
        IMethod[] methods = type.getMethods();
        Set<String> methodLookup = new HashSet<>();

        for (IMethod method : methods) {
            if (method.getParameterNames().length == 0
                    && (method.getElementName().startsWith("get") || method.getElementName().startsWith("is"))) {
                methodLookup.add(method.getElementName());
            }
        }

        return methodLookup;
    }

    private boolean isBooleanField(IField field) throws JavaModelException {
        return BOOLEAN_TYPE_SIGNATURES.contains(field.getTypeSignature());
    }

    private String capitializeFirstLetter(String name) {
        String result = name;

        if(!name.isEmpty()) {
            String first = new String(name.substring(0, 1));
            result = name.replaceFirst(first, first.toUpperCase());
        }

        return result;
    }
}
