package com.tendio.kdt.executor.actions.model;

import com.google.common.collect.Lists;
import com.tendio.kdt.configurator.InvalidConfigurationException;
import com.tendio.kdt.configurator.model.TestSuiteParameters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class ActionFactory {
    private static final String PARAMETERS_REPLACEMENT_STRING = "\"(.*?)\"";
    private static final String PARAMETERS_REPLACEMENT = "\"\"";
    private static final Logger LOGGER = LogManager.getLogger();
    private static final List<ActionMethod> REGISTRY = Lists.newArrayList();

    private ActionFactory() {
    }

    public static List<ActionMethod> getRegistry() {
        return REGISTRY;
    }

    public static ActionMethod createStepByDefinition(String definition) {
        ActionMethod method = ActionFactory.getRegisteredActionMethod(definition);
        Object[] parameters = ActionFactory.getParameters(definition);
        //resolveParameters(parameters);

        return new ActionMethod(method.getMethod(), definition, parameters);
    }

/*    public static CompositeStep resolveCompositeStep(String definition) {
        ActionMethod method = ActionFactory.getRegisteredActionMethod(definition);
        Object[] parameters = ActionFactory.getParameters(definition);
        //resolveParameters(parameters);

        return new ActionMethod(method.getMethod(), definition, parameters);
    }*/

    //TODO: improve logic across with different ParametersHandler classes
    public static String resolveTestSuiteParameters(String str) {
        String value = TestSuiteParameters.getParameter(str);
        if (value != null) {
            return value;
        }

        //handling case for actions like: \"Param1=Value1\"
        /*final String replacedKey = key.replaceAll("\\$\\{", "").replaceAll("}", "");
        for (String singleParam : replacedKey.split("(=)|(>)|(->)")) {
            String parameter = TestSuiteParameters.getParameter(singleParam);
            if (parameter != null) {
                value = replacedKey.replaceAll(singleParam, parameter);
            }
        }
        return value != null ? value : key;*/
        final String parameterPattern = "\\$\\{\\w+\\}";
        Pattern p = Pattern.compile(parameterPattern);
        Matcher m = p.matcher(str);

        while (m.find()) {
            String matched = m.group();
            String parameter = matched.substring(2, m.group().length() - 1);
            String val = TestSuiteParameters.getParameter(parameter);
            if (val != null) {
                str = str.replace(matched, val);
            }
        }
        return str;
    }

    private static ActionMethod getRegisteredActionMethod(String mappingDefinition) {
        for (ActionMethod method : REGISTRY) {
            String annotationDefinition = method.getMappingDefinition().replaceAll(PARAMETERS_REPLACEMENT_STRING, PARAMETERS_REPLACEMENT);
            String actualMappingDefinition = mappingDefinition.replaceAll(PARAMETERS_REPLACEMENT_STRING, PARAMETERS_REPLACEMENT);
            if (actualMappingDefinition.equalsIgnoreCase(annotationDefinition)) {
                LOGGER.debug("Found registered Action Method: {} for definition {}", method.getMethod(), mappingDefinition);
                return method;
            }
        }

        String message = String.format("Couldn`t find registered Action Method for definition: %s", mappingDefinition);
        LOGGER.error(message);
        throw new InvalidConfigurationException(message);
    }

    // TODO: support not only Strings!
    //TODO: normal parameters reader!!!
    private static Object[] getParameters(String stepDefinition) {
        List<Object> parameters = Lists.newArrayList();
        Pattern p = Pattern.compile(ActionFactory.PARAMETERS_REPLACEMENT_STRING);
        Matcher m = p.matcher(stepDefinition);
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            parameters.add(stepDefinition.substring(start + 1, end - 1));
        }

        return parameters.toArray();
    }

    public static void resolveParameters(Object[] parameters) {
        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = resolveTestSuiteParameters((String) parameters[i]);
        }
    }

}
