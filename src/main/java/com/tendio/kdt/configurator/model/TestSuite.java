package com.tendio.kdt.configurator.model;

import java.util.ArrayList;

public class TestSuite {
    private String id;
    private ArrayList<TestCase> testCases;
    private TestSuiteParameters parameters;

    public TestSuite(String id, ArrayList<TestCase> testCases, TestSuiteParameters parameters) {
        this.id = id;
        this.testCases = testCases;
        this.parameters = parameters;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<TestCase> getTestCases() {
        return testCases;
    }

    @Override
    public String toString() {
        return "TestSuite{" +
                "id='" + id + '\'' +
                ", testCases=" + testCases +
                ", parameters=" + parameters +
                '}';
    }

}
