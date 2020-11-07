package com.tendio.kdt.configurator.model;

import com.tendio.kdt.executor.actions.model.ActionFactory;
import com.tendio.kdt.executor.actions.model.ActionMethod;
import com.tendio.kdt.executor.exception.InterruptedTestCaseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Step extends AbstractStep {
    private static final Logger LOGGER = LogManager.getLogger();
    private boolean isCurrentlyExecuting;
    private String description;
    private ActionMethod actionMethod;
    //passed by default
    private boolean isPassed = true;

    public Step(String description, String actionDefinition) {
        this.description = description;
        this.actionMethod = ActionFactory.createStepByDefinition(actionDefinition);
    }

    boolean isCurrentlyExecuting() {
        return isCurrentlyExecuting;
    }

    public void setCurrentlyExecuting(boolean currentlyExecuting) {
        isCurrentlyExecuting = currentlyExecuting;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ActionMethod getActionMethod() {
        return actionMethod;
    }

    public void setActionMethod(ActionMethod actionMethod) {
        this.actionMethod = actionMethod;
    }

    public boolean isPassed() {
        return isPassed;
    }

    public void setPassed(boolean isPassed) {
        this.isPassed = isPassed;
    }

    @Override
    public String toString() {
        return "Step{" +
                "description='" + description + '\'' +
                ", actionMethod=" + actionMethod +
                ", isPassed=" + isPassed +
                '}';
    }

    @Override
    public void execute() {
        try {
            LOGGER.debug("Executing step: {} ...", this);
            setCurrentlyExecuting(true);
            actionMethod.execute();
            setCurrentlyExecuting(false);
            LOGGER.debug("Successfully executed step: {}", this);
        } catch (Exception e) {
            isPassed = false;
            throw new InterruptedTestCaseException(e.getCause());
        }
    }

}
