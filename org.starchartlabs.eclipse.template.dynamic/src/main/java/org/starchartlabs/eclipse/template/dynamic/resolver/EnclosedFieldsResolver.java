/*
 * Copyright (c) Feb 8, 2019 StarChart Labs Authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    romeara - initial API and implementation and/or initial documentation
 */
package org.starchartlabs.eclipse.template.dynamic.resolver;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.corext.template.java.CompilationUnitContext;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

/**
 * Fields resolver which allows defining a template to fill for every Java field
 *
 * <p>
 * The template variable resolved by this class is expected to be of the form:
 *
 * <pre>
 * ${id:enclosed_fields(template, separator)}
 * </pre>
 *
 * Where the user-provided values are:
 * <ul>
 * <li>id - identifier (unique within template) and default filler value in case of error</li>
 * <li>template - Line to substitute per bean-field. May use ${type} within to substitute field type, ${name} within to
 * substitute field name</li>
 * <li>separator - value, if any, to place between each occurrence of the substituted template. May use ${newline} to
 * substitute in a System.lineSeparator(). Resulting new-lines will be overridden by code formatter if it is enabled for
 * the template</li>
 * </ul>
 *
 * <p>
 * This class is intended to be extensible
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
 * @since 0.3.0
 */
@SuppressWarnings("restriction")
public class EnclosedFieldsResolver extends TemplateVariableResolver {

    protected static final String TYPE_PLACEHOLDER = "\\$\\{type\\}";

    protected static final String NAME_PLACEHOLDER = "\\$\\{name\\}";

    protected static final String NEWLINE_PLACEHOLDER = "\\$\\{newline\\}";

    @Override
    public void resolve(TemplateVariable variable, TemplateContext context) {
        if (context instanceof CompilationUnitContext) {
            CompilationUnitContext jc = (CompilationUnitContext) context;

            String[] bindings = resolveAll(jc, variable.getVariableType().getParams());

            // Store the result, and if resolved set unambiguous to true to avoid trying to get user input
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

    /**
     * Returns all possible bindings available in <code>context</code>, taking into account variable parameters
     *
     * @param context
     *            The Java compilation unit context in which to resolve the type
     * @param variableParameters
     * @return an array of possible bindings of this type in <code>context</code>, null if binding was unsuccessful
     */
    protected String[] resolveAll(CompilationUnitContext context, List<String> variableParameters) {
        String[] result = null;

        if (variableParameters.size() == 2) {
            String template = variableParameters.get(0);
            String separator = variableParameters.get(1).replaceAll(NEWLINE_PLACEHOLDER, System.lineSeparator());

            List<String> lines = new ArrayList<>();

            for (Entry<String, String> entry : getFieldTypes(context).entrySet()) {
                String processed = template
                        .replaceAll(TYPE_PLACEHOLDER, entry.getValue())
                        .replaceAll(NAME_PLACEHOLDER, entry.getKey());

                lines.add(processed);
            }

            String value = lines.stream().collect(Collectors.joining(separator));
            result = new String[] { value };
        }

        return result;
    }

    /**
     * Extracts field type information from a compilation unit
     *
     * @param context
     *            Information about the compilation unit the template is being inserted into
     * @return A mapping of any bean fields to their Java type
     */
    protected Map<String, String> getFieldTypes(CompilationUnitContext context) {
        Objects.requireNonNull(context);

        Map<String, String> result = new LinkedHashMap<>();

        try {
            IType type = (IType) context.findEnclosingElement(IJavaElement.TYPE);

            for (IField field : type.getFields()) {
                result.put(field.getElementName(), Signature.getSignatureSimpleName(field.getTypeSignature()));
            }
        } catch (JavaModelException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

}
