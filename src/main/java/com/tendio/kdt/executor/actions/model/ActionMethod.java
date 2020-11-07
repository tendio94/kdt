package com.tendio.kdt.executor.actions.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.util.Arrays;

public final class ActionMethod {
    private static final Logger LOGGER = LogManager.getLogger(ActionMethod.class);
    private Method method;
    private String mappingDefinition;
    private Object[] parameters;

    ActionMethod(String mappingDefinition, Method method) {
        this.method = method;
        this.mappingDefinition = mappingDefinition;
    }

    ActionMethod(Method method, String mappingDefinition, Object[] parameters) {
        this.method = method;
        this.mappingDefinition = mappingDefinition;
        this.parameters = parameters;
    }

    public void execute() throws ReflectiveOperationException {
        ActionFactory.resolveParameters(parameters);
        Object instance = this.method.getDeclaringClass().newInstance();
        LOGGER.debug("Invoking method {} with parameters: {}", method, Arrays.toString(parameters));
        this.method.invoke(instance, parameters);
        LOGGER.debug("Successfully invoked method {} with parameters: {}", method, Arrays.toString(parameters));
    }

    public String getMappingDefinition() {
        return mappingDefinition;
    }

    public Method getMethod() {
        return method;
    }

    @Override
    public String toString() {
        return "ActionMethod{" +
                "method=" + method +
                ", mappingDefinition='" + mappingDefinition + '\'' +
                ", parameters=" + Arrays.toString(parameters) +
                '}';
    }
}
