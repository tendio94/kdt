package com.tendio.kdt;

import com.tendio.kdt.executor.browser.Browser;
import org.apache.logging.log4j.LogManager;
import org.openqa.selenium.UnexpectedAlertBehaviour;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.Properties;

public final class TestProperties {
    private static final String CONFIG_FILE = "test.properties";
    private static final Properties PROPERTIES = new Properties();
    private static boolean isReportingEnabled = Boolean.parseBoolean(getProperty("test.reporting.enabled"));

    static {
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            PROPERTIES.load(input);
        } catch (IOException e) {
            LogManager.getLogger().error(e.getMessage());
        }
    }

    private TestProperties() {
    }

    public static void setLog4jConfigLocation() {
        String log4jProperties = TestProperties.getProperty("test.log4j.properties.path");
        System.setProperty("log4j.configurationFile", (log4jProperties != null ? log4jProperties : ""));
    }

    @Nullable
    public static UnexpectedAlertBehaviour getUnexpectedAlertBehavior() {
        String value = TestProperties.getProperty("test.browser.unexpected.alert.behavior");
        return UnexpectedAlertBehaviour.fromString(value);
    }

    public static Browser.Type getBrowserType() {
        return Browser.Type.valueOf(Objects.requireNonNull(getProperty("test.browser.type")).toUpperCase());
    }

    public static String getDriverBinaryPath() {
        String propertyKey = "test." + getBrowserType().name().toLowerCase() + ".binary.path";
        return getProperty(propertyKey);
    }

    public static boolean isRemoteExecution() {
        String propertyValue = getProperty("test.remote.execution");
        return Boolean.valueOf(propertyValue);
    }

    public static boolean isReportingEnabled() {
        return isReportingEnabled;
    }

    public static void setReportingEnabled(boolean isEnabled) {
        isReportingEnabled = isEnabled;
    }

    public static URL getRemoteHubUrl() {
        if (!isRemoteExecution()) {
            return null;
        }

        URL url = null;
        try {
            url = new URL(Objects.requireNonNull(getProperty("test.remote.hub.url")));
        } catch (MalformedURLException ignored) {
        }
        return url;
    }

    /**
     * @return null if property is not defined in test.properties file
     */
    @Nullable
    public static String getProperty(@Nonnull String key) {
        return PROPERTIES.getProperty(key);
    }
}
