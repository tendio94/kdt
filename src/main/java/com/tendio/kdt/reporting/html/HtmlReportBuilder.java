package com.tendio.kdt.reporting.html;

import com.google.common.base.Throwables;
import com.tendio.kdt.reporting.AbstractReportBuilder;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;

public class HtmlReportBuilder extends AbstractReportBuilder {
    public static final String HTML_DOCUMENT_HEAD = "<html title=\"%s\"><head><style>img.resize {max-width:1500px;"
            + "max-height:900px;} tr.red {background-color:red;} tr.green {background-color:lightgreen;} "
            + "tr.yellow {background-color:yellow;} .timestamp {text-align:left;} .link {text-align: left;}"
            + "</style></head><body>";
    private static final String HTML_ROW_STRING = "<tr class='%s'><td><div class='timestamp'>%s</div>%s</td><td>%s</td><td>%s</td></tr>";
    private static final String HTML_TABLE_STRING = "<table><th>Message</th><th>Description</th><th>Screenshot</th>";
    private static final String HTML_DOCUMENT_CLOSURE = "</table></body></html>";

    public static File saveScreenshot(File file) {
        File saved = new File("./report/screenshots/" + file.getName());
        try {
            FileUtils.moveFile(file, saved);
        } catch (IOException e) {
            LOGGER.error("Exception occurred: {}. Cause: {}. Stacktrace: {}",
                    e.getMessage(), e.getCause(), Throwables.getStackTraceAsString(e));
            saved = null;
        }
        return saved;
    }

    private static String getImgSrc(File file) {
        return "<img class='resize' src=\"screenshots/" + file.getName() + "\" alt=\"No worries!\">";
    }

    String createHtmlDocumentHead(String title) {
        return getSb().append(String.format(HTML_DOCUMENT_HEAD, title)).append(HTML_TABLE_STRING).toString();
    }

    String closeHtmlDocument() {
        return getSb().append(HTML_DOCUMENT_CLOSURE).toString();
    }

    @Override
    protected String appendSection(String stepDescription, String message, @Nullable File screenshot, LogLevel logLevel) {
        String screenshotPath = "No screenshot provided";
        if (screenshot != null) {
            screenshotPath = getImgSrc(screenshot);
        }

        String sectionInfo = String.format(HTML_ROW_STRING, logLevel.getLogColor(),
                new Timestamp(System.currentTimeMillis()), stepDescription, removeMarkupElements(message), screenshotPath);
        getSb().append(sectionInfo);
        return sectionInfo;
    }

    //TODO: use regexp
    private String removeMarkupElements(String s) {
        //removing all tags that may break report structure
        return s.replaceAll("<td>", "")
                .replaceAll("</td>", "")
                .replaceAll("<tr>", "")
                .replaceAll("</tr>", "")
                .replaceAll("<table>", "")
                .replaceAll("</table>", "");
    }
}
