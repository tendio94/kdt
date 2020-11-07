package com.tendio.kdt.reporting.html;

import com.tendio.kdt.TestProperties;
import com.tendio.kdt.configurator.model.Step;
import com.tendio.kdt.executor.browser.Browser;
import com.tendio.kdt.executor.exception.InterruptedTestCaseException;
import com.tendio.kdt.reporting.Report;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class HtmlReport extends Report {
    //needed for single-thread execution
    private boolean hasBeenClosed;

    public HtmlReport(String id) {
        super(id);
    }

    @Override
    public void writeReport(String data) {
        try {
            FileUtils.write(getReportFile(), data, Charset.defaultCharset(), false);
        } catch (IOException e) {
            throw new InterruptedTestCaseException(e.getMessage(), e);
        }
    }

    @Override
    public void create() {
        deleteFileIfExists(getReportFile());
        if (!isCreated) {
            if (builder == null) {
                builder = new HtmlReportBuilder();
            }
            String head = ((HtmlReportBuilder) builder)
                    .createHtmlDocumentHead(getTestCase() != null ? getTestCase().getId() : "KDT Automation Tool");
            writeReport(head);
            //TODO: one more reminder to change Reporting module architecture
        }
    }

    @Override
    public void close() {
        String closure = ((HtmlReportBuilder) builder).closeHtmlDocument();
        writeReport(closure);
        AggregatedHtmlReport.addReportInfo(this);

        //TODO: remove threadlocal report - it`s not maintainable architecture
        //manipulations because of threadlocal report
        builder = new HtmlReportBuilder();
        hasBeenClosed = true;
        isCreated = false;
    }

    @Override
    public void warn(String message, @Nullable File screenshot) {
        if (TestProperties.isReportingEnabled()) {
            log(message, screenshot, LogLevel.WARN);
        }
    }

    @Override
    public void error(String message, @Nullable File screenshot) {
        if (TestProperties.isReportingEnabled()) {
            log(message, screenshot, LogLevel.ERROR);
        }
    }

    @Override
    public void info(String message, @Nullable File screenshot) {
        if (TestProperties.isReportingEnabled()) {
            log(message, screenshot, LogLevel.INFO);
        }
    }

    @Override
    public void log(String stepDescription, String message, @Nullable File screenshot, @Nonnull LogLevel logLevel) {
        if (TestProperties.isReportingEnabled()) {
            if (hasBeenClosed && !isCreated) {
                create();
            }

            String section = ((HtmlReportBuilder) builder).appendSection(stepDescription, message, screenshot, logLevel);
            writeReport(section);
        }
    }

    @Override
    public String toString() {
        return "HtmlReport{" +
                "status=" + getStatus() +
                ", reportName='" + reportName + '\'' +
                '}';
    }

    private void deleteFileIfExists(File file) {
        if (file.exists() && file.isFile()) {
            boolean isDeleted = FileUtils.deleteQuietly(file);
            if (isDeleted) {
                LogManager.getLogger().debug("Successfully deleted previous report: {}", file.getAbsolutePath());
            }
        }
    }

    private File getReportFile() {
        String filepath = getReportPath() + FOLDER_PATH_SEPARATOR + reportName + HTML_EXTENSION;
        return new File(filepath);
    }

    private void log(String message, @Nullable File screenshot, LogLevel logLevel) {
        if (hasBeenClosed && !isCreated) {
            create();
        }

        Step step = getTestCase().getCurrentlyExecutingStep();
        String stepDescription = null;

        //TODO: what if step is null? is it possible at all?
        if (step != null) {
            stepDescription = step.getDescription();
            if (logLevel == LogLevel.ERROR) {
                step.setPassed(false);
            }
        }

        //TODO: we should not access browser FROM REPORT
        if (Browser.getBrowser() != null) {
            String link = Browser.getBrowser().getCurrentUrl();
            stepDescription = String.format("<div class='link'><a href='%s'>Link</a></div>", link) + stepDescription;
        }

        String section = ((HtmlReportBuilder) builder).appendSection(message, stepDescription, screenshot, logLevel);
        writeReport(section);
    }

}
