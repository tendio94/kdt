package com.tendio.kdt.reporting;

import com.google.common.collect.Maps;
import com.tendio.kdt.TestProperties;
import com.tendio.kdt.configurator.model.Step;
import com.tendio.kdt.configurator.model.TestCase;
import com.tendio.kdt.reporting.html.HtmlReport;
import com.tendio.kdt.reporting.html.LogLevel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.time.Instant;
import java.util.Map;

//** An abstract point of access to Report functionality (currently only html) **/
public abstract class Report {
    public static final String HTML_EXTENSION = ".html";
    public static final String FOLDER_PATH_SEPARATOR = "/";
    private static final String REPORT_FOLDER = TestProperties.getProperty("test.report.folder");
    private static Map<String, HtmlReport> reports = Maps.newConcurrentMap();
    protected AbstractReportBuilder builder;
    protected String reportName;
    protected boolean isCreated = false;
    private TestCase testCase;
    private String body;

    protected Report(String id) {
        if (TestProperties.isReportingEnabled()) {
            this.reportName = id;
            create();
            isCreated = true;
            reports.put(reportName, (HtmlReport) this);
        }
    }

    public static String getReportPath() {
        return REPORT_FOLDER;
    }

    public static HtmlReport getReport() {
        if (TestProperties.isReportingEnabled()) {
            TestCase current = TestCase.getCurrentlyExecuting();
            if (current != null) {
                HtmlReport report = reports.get(current.getId());
                return report != null ? report : new HtmlReport(current.getId());
            }
            //TODO: figure out how to handle wisely
            // while creating report not from framework
            return new HtmlReport(Instant.now().toString());
        } else {
            //dummy not to break API
            return new HtmlReport("dummy");
        }
    }

    public boolean isCreated() {
        return isCreated;
    }

    public Status getStatus() {
        for (Step s : getTestCase().getSteps()) {
            if (!s.isPassed()) {
                return Status.FAILED;
            }
        }
        return Status.PASSED;
    }

    public TestCase getTestCase() {
        return testCase;
    }

    public void setTestCase(TestCase testCase) {
        this.testCase = testCase;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public AbstractReportBuilder getBuilder() {
        return builder;
    }

    public void setBuilder(AbstractReportBuilder builder) {
        this.builder = builder;
    }

    public void info(String message) {
        info(message, null);
    }

    public void warn(String message) {
        warn(message, null);
    }

    public void error(String message) {
        error(message, null);
    }

    public abstract void create();

    public abstract void close();

    public abstract void log(String stepDescription, String message, @Nullable File screenshot, @Nonnull LogLevel logLevel);

    public abstract void info(String message, @Nullable File screenshot);

    public abstract void warn(String message, @Nullable File screenshot);

    public abstract void error(String message, @Nullable File screenshot);

    public abstract void writeReport(String data);

    public enum Status {
        PASSED, FAILED, WARN
    }

}
