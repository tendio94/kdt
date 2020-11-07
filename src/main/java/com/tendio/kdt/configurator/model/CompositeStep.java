package com.tendio.kdt.configurator.model;

import com.google.common.collect.Lists;

import java.util.LinkedList;

/**
 * Represents an actions sequence aggregated in one step.
 * Initially designed to avoid copy-paste and multiple repetitions of the atomic steps
 */
public class CompositeStep extends AbstractStep {
    private String definition;
    private String description;
    private LinkedList<Step> steps;

    public CompositeStep(String description, String definition) {
        this.description = description;
        this.definition = definition;
        this.steps = Lists.newLinkedList();

    }

    public String getDescription() {
        return description;
    }

    public String getDefinition() {
        return definition;
    }

    public LinkedList<Step> getSteps() {
        return steps;
    }

    @Override
    public void execute() {
        steps.forEach(Step::execute);
    }
}
