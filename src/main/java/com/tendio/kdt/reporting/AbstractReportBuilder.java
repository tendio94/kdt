package com.tendio.kdt.reporting;

import com.tendio.kdt.reporting.html.LogLevel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;

public abstract class AbstractReportBuilder {
    protected static final Logger LOGGER = LogManager.getLogger();
    private StringBuilder sb;

    public StringBuilder getSb() {
        if (sb == null) {
            sb = new StringBuilder();
        }
        return sb;
    }

    public String getBody() {
        return this.sb.toString();
    }

    protected abstract String appendSection(String stepDescription,
                                            String message, @Nullable File screenshot, LogLevel logLevel);

}
