package com.tendio.kdt.executor.browser;

import com.tendio.kdt.TestProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.tendio.kdt.executor.browser.Browser.Type.*;

final class DriverFactory {
    private static final Logger LOGGER = LogManager.getLogger();
    //WORKAROUND cause InternetExplorerOptions has no setBinary(String path) method :(((
    private static final Map<Browser.Type, String> BROWSER_TYPE_TO_PROPERTY = new HashMap<Browser.Type, String>() {{
        put(FIREFOX, "webdriver.gecko.driver");
        put(CHROME, "webdriver.chrome.driver");
        put(IE, "webdriver.ie.driver");
    }};

    static {
        System.setProperty("webdriver.firefox.bin", "C:\\Program_Dev\\Mozilla Firefox\\firefox.exe");
    }

    private DriverFactory() {
    }

    static WebDriver instantiateDriver() {
        Browser.Type type = TestProperties.getBrowserType();
        return instantiateDriver(type);
    }

    private static WebDriver instantiateDriver(Browser.Type type) {
        return TestProperties.isRemoteExecution() ? initRemoteDriver(type) : initLocalDriver(type);
    }

    private static WebDriver initRemoteDriver(Browser.Type type) {
        DesiredCapabilities capabilities;
        URL remoteHubUrl = Objects.requireNonNull(TestProperties.getRemoteHubUrl());
        switch (type) {
            case IE:
                capabilities = DesiredCapabilities.internetExplorer();
                break;
            case CHROME:
                capabilities = DesiredCapabilities.chrome();
                break;
            default:
                capabilities = DesiredCapabilities.firefox();
                break;
        }

        return new RemoteWebDriver(remoteHubUrl, capabilities);
    }

    private static WebDriver initLocalDriver(Browser.Type type) {
        String binaryPath = TestProperties.getDriverBinaryPath();
        File file = new File(binaryPath);
        System.setProperty(BROWSER_TYPE_TO_PROPERTY.get(type), file.getAbsolutePath());
        LOGGER.debug("Instantiating driver for thread {} : browser type: {}; binary executable file: {} ...",
                Thread.currentThread().getName(), type.name(), binaryPath);

        WebDriver driver;
        //final UnexpectedAlertBehaviour alertBehaviour = TestProperties.getUnexpectedAlertBehavior();
        switch (type) {
            case IE:
                InternetExplorerOptions ieOptions = new InternetExplorerOptions();
                ieOptions.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
                //ieOptions.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, alertBehaviour);
                driver = new InternetExplorerDriver(ieOptions);
                break;
            case CHROME:
                ChromeOptions chromeOptions = new ChromeOptions();
                //chromeOptions.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, alertBehaviour);
                chromeOptions.setExperimentalOption("useAutomationExtension", false);
                driver = new ChromeDriver(chromeOptions);
                break;
            default:
                FirefoxOptions ffOptions = new FirefoxOptions();
                ffOptions.setCapability("marionette", true);
                //ffOptions.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, alertBehaviour);
                driver = new FirefoxDriver(ffOptions);
                break;
        }
        LOGGER.debug("Successfully instantiated driver for thread {}" +
                        " : browser type: {}; binary executable file: {} ...",
                Thread.currentThread().getName(), type.name(), binaryPath);
        LOGGER.debug("Driver info: {}", driver);

        applyDefaultConfiguration(driver);
        return driver;
    }

    //TODO: add specific capabilities&settings for each driver type
    private static void applyDefaultConfiguration(WebDriver driver) {
        driver.manage().window().maximize();
        LOGGER.debug("Maximized browser {} window size. Dimensions: {}",
                driver, driver.manage().window().getSize());

        final long waiter =
                Long.parseLong(Objects.requireNonNull(TestProperties.getProperty("test.browser.waiter")));
        driver.manage().timeouts().implicitlyWait(waiter, TimeUnit.SECONDS);
        LOGGER.debug("Set implicit waiter for browser {} to {} seconds", driver, waiter);

        final long scriptTimeout =
                Long.parseLong(Objects.requireNonNull(TestProperties.getProperty("test.browser.script.timeout")));
        driver.manage().timeouts().setScriptTimeout(scriptTimeout, TimeUnit.SECONDS);
        LOGGER.debug("Set scripts timeout for browser {} to {} seconds", driver, scriptTimeout);
    }

}
