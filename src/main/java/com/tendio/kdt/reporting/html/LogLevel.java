package com.tendio.kdt.reporting.html;

public enum LogLevel {
    INFO("green"),
    WARN("yellow"),
    ERROR("red");

    private String logColor;

    LogLevel(String logColor) {
        this.logColor = logColor;
    }

    public String getLogColor() {
        return logColor;
    }


}
