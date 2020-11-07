package com.tendio.kdt.reporting.html;

import com.google.common.collect.Lists;
import com.tendio.kdt.reporting.Report;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.List;

public final class AggregatedHtmlReport {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String HTML_TABLE_STRING = "<table><th>.</th><th>Test Case ID</th><th>Status</th><th>Details</th>";
    private static final String HTML_ROW_STRING = "<tr class='%s'><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>";
    private static final List<Info> reportsInfo = Lists.newLinkedList();
    private static int rowsCount = 0;
    private String id;
    private Duration executionTime;
    private HtmlReportBuilder builder = new HtmlReportBuilder();

    public AggregatedHtmlReport() {
    }

    public static List<Info> getReportsInfo() {
        return reportsInfo;
    }

    public static synchronized void addReportInfo(HtmlReport report) {
        reportsInfo.add(new Info(report.getStatus(), report.getTestCase().getId()));
        LOGGER.debug("Added report {} for aggregated summary reporting", report);
    }

    public Duration getExecutionTime() {
        return executionTime;
    }

    public String getId() {
        return id;
    }

    public HtmlReportBuilder getBuilder() {
        return builder;
    }

    public AggregatedHtmlReport withId(@Nonnull String id) {
        this.id = id;
        return this;
    }

    public AggregatedHtmlReport withExecutionTime(@Nonnull Duration duration) {
        this.executionTime = duration;
        return this;
    }

    public AggregatedHtmlReport build() {
        builder.getSb().append(HtmlReportBuilder.HTML_DOCUMENT_HEAD);
        builder.getSb().append("<h3>Execution time: ").append(executionTime).append("</h3>");
        builder.getSb().append(HTML_TABLE_STRING);
        reportsInfo.forEach(this::appendReportRow);
        builder.closeHtmlDocument();
        return this;
    }

    private void appendReportRow(Info info) {
        String rowColor = getRowColorForStatus(info.getStatus());
        String testCaseId = info.getTestCaseId();
        String detailsLink = String.format("<a href='%s.html'>%s</a>", testCaseId, testCaseId);
        builder.getSb().append(String.format(HTML_ROW_STRING, rowColor, ++rowsCount, testCaseId, info.getStatus(), detailsLink));
    }

    public void write() {
        String filepath = Report.getReportPath() + Report.FOLDER_PATH_SEPARATOR + id + Report.HTML_EXTENSION;
        try {
            LOGGER.debug("Writing aggregated report: filepath={} id={} and reportsInfo: {} ...", filepath, id, getReportsInfo());
            FileUtils.write(new File(filepath), builder.getBody(), Charset.defaultCharset());
            LOGGER.debug("Successfully wrote aggregated report: filepath={} id={} ...", filepath, id);
        } catch (IOException e) {
            LOGGER.error("Could not write aggregated report: filepath={} id={}", filepath, id);
        }
    }

    private String getRowColorForStatus(Report.Status status) {
        switch (status) {
            case FAILED:
                return LogLevel.ERROR.getLogColor();
            case WARN:
                return LogLevel.WARN.getLogColor();
            default:
                return LogLevel.INFO.getLogColor();
        }
    }

    //stores only minimum info required for summary reporting
    private static class Info {
        private Report.Status status;
        private String testCaseId;

        public Info(Report.Status status, String testCaseId) {
            this.status = status;
            this.testCaseId = testCaseId;
        }

        @Override
        public String toString() {
            return "ReportInfo{" +
                    "status=" + status +
                    ", testCaseId='" + testCaseId + '\'' +
                    '}';
        }

        public Report.Status getStatus() {
            return status;
        }

        public String getTestCaseId() {
            return testCaseId;
        }
    }

}
