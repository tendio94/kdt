package com.tendio.kdt.configurator.model;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.tendio.kdt.executor.browser.Browser;
import com.tendio.kdt.executor.exception.InterruptedTestCaseException;
import com.tendio.kdt.reporting.Report;
import com.tendio.kdt.reporting.html.HtmlReport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.StaleElementReferenceException;

import javax.annotation.Nullable;
import java.io.File;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;

public class TestCase implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger();
    private static Map<Thread, TestCase> caseMap = Maps.newConcurrentMap();
    private Report report;
    private String id;
    private LinkedList<Step> steps;

    public TestCase(LinkedList<Step> steps, String id) {
        this.id = id;
        this.steps = steps;
    }

    @Nullable
    public static TestCase getCurrentlyExecuting() {
        return caseMap.get(Thread.currentThread());
    }

    @Nullable
    public Step getCurrentlyExecutingStep() {
        for (Step step : steps) {
            if (step.isCurrentlyExecuting()) {
                return step;
            }
        }
        return null;
    }

    @Override
    public void run() {
        try {
            report = new HtmlReport(id);
            report.setTestCase(this);
            caseMap.put(Thread.currentThread(), this);
            LOGGER.debug("Created instance of {} for thread: {} with ID={}",
                    report.getClass(), Thread.currentThread().getName(), id);
            steps.forEach(AbstractStep::execute);
        } catch (NoSuchElementException | StaleElementReferenceException | InterruptedTestCaseException e) {
            LOGGER.error("Exception occurred: {}. Cause: {}. Stacktrace: {}",
                    e.getMessage(), e.getCause(), Throwables.getStackTraceAsString(e));
            File screenshot = Browser.getBrowser() != null ? Browser.getScreenshot() : null;
            report.error(e.getCause().getMessage(), screenshot);
        } catch (Exception e) {
            LOGGER.error("Exception occurred: {}. Cause: {}. Stacktrace: {}",
                    e.getMessage(), e.getCause(), Throwables.getStackTraceAsString(e));
            report.error(e.getMessage(), null);
        } finally {
            if (Browser.getBrowser() != null) {
                Browser.getBrowser().quit();
                LOGGER.debug("Successfully closed browser {} for thread: {}",
                        Browser.getBrowser(), Thread.currentThread().getName());
            }
            Step step = report.getTestCase().getCurrentlyExecutingStep();
            if (step != null) {
                step.setCurrentlyExecuting(false);
            }
            report.close();
        }
    }

    @Override
    public String toString() {
        return "TestCase{" +
                "id='" + id + '\'' +
                ", steps=" + steps +
                '}';
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LinkedList<Step> getSteps() {
        return steps;
    }

    public void setSteps(LinkedList<Step> steps) {
        this.steps = steps;
    }

}

