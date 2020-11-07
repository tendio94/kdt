package com.tendio.kdt.executor.browser;

import com.google.common.collect.Lists;
import com.tendio.kdt.executor.actions.impl.CommonActions;
import com.tendio.kdt.reporting.html.HtmlReportBuilder;
import com.tendio.kdt.web.ui.model.base.CustomWebElement;
import com.tendio.kdt.web.ui.model.base.WebElementImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class Browser {
    private static final Logger LOGGER = LogManager.getLogger();
    private static ThreadLocal<WebDriver> browser = new ThreadLocal<>();

    private Browser() {
    }

    public static WebDriver getBrowser() {
        return browser.get();
    }

    public static void setBrowser(WebDriver driver) {
        browser.set(driver);
    }

    public static WebDriver initBrowser() {
        if (getBrowser() == null || isQuit(getBrowser())) {
            WebDriver driver = DriverFactory.instantiateDriver();
            browser.set(driver);
            LOGGER.debug("Instantiated new browser driver {} for thread: {}",
                    driver, Thread.currentThread().getName());
            return driver;
        } else {
            LOGGER.debug("Driver {} has been already instantiated for thread: {} ",
                    browser.get(), Thread.currentThread().getName());
            return Browser.getBrowser();
        }
    }

    @Nullable
    public static File getScreenshot() {
        try {
            File screenshot = ((TakesScreenshot) browser.get()).getScreenshotAs(OutputType.FILE);
            return HtmlReportBuilder.saveScreenshot(screenshot);
        } catch (UnhandledAlertException e) {
            CommonActions.acceptAlertWithReporting();
        } catch (Exception ignored) {
        }
        return null;
    }

    public static boolean isQuit(WebDriver driver) {
        return ((RemoteWebDriver) driver).getSessionId() == null;
    }

    public static <T extends CustomWebElement> T get(Class<T> clazz, @Nullable SearchContext parent,
                                                     String name, int number) {
        T instance = WebElementImpl.Constructor.getNewInstanceViaDefaultConstructor(clazz);
        instance.setDriver(getBrowser());
        return WebElementImpl.Constructor.getElementByLocatorsInsideParent(instance, parent, number, name);
    }

    public static <T extends CustomWebElement> T get(Class<T> clazz, @Nullable SearchContext parent, int number) {
        T element = WebElementImpl.Constructor.getNewInstanceViaDefaultConstructor(clazz);
        element.setDriver(getBrowser());
        return WebElementImpl.Constructor.getElementByLocatorsInsideParent(element, parent, number, null);
    }

    public static <T extends CustomWebElement> T get(Class<T> clazz, @Nullable SearchContext parent, String name) {
        if (WebElementImpl.MixedLocator.isMixedLocator(name)) {
            WebElementImpl.MixedLocator mixedLocator = WebElementImpl.MixedLocator.fromString(name);
            return get(clazz, parent, mixedLocator.getName(), mixedLocator.getNumber());
        }

        T element = WebElementImpl.Constructor.getNewInstanceViaDefaultConstructor(clazz);
        element.setDriver(getBrowser());
        String xpath = String.format(element.getFindByNameXpath(), name);
        SearchContext searchContext = (parent != null) ? parent : getBrowser();
        element.setElement(searchContext.findElement(By.xpath(xpath)));
        return element;
    }

    public static <T extends CustomWebElement> T get(Class<T> clazz, @Nullable SearchContext parent) {
        T element = WebElementImpl.Constructor.getNewInstanceViaDefaultConstructor(clazz);
        element.setDriver(getBrowser());
        String xpath = element.getElementContainerXpath();
        SearchContext searchContext = (parent != null) ? parent : getBrowser();
        element.setElement(searchContext.findElement(By.xpath(xpath)));
        return element;
    }

    public static <T extends CustomWebElement> List<T> getAll(Class<T> clazz, @Nullable SearchContext parent) {
        List<T> toReturn = Lists.newLinkedList();
        String xpath = WebElementImpl.Constructor.getNewInstanceViaDefaultConstructor(clazz).getElementContainerXpath();
        SearchContext searchContext = (parent != null) ? parent : getBrowser();
        List<WebElement> elements = searchContext.findElements(By.xpath(xpath));

        elements.forEach(e -> {
            T element = WebElementImpl.Constructor.construct(clazz, getBrowser(), e);
            toReturn.add(element);
        });
        return toReturn;
    }

    public static void applyWaiter(int seconds) {
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(seconds));
        } catch (InterruptedException ignored) {
        }
    }

    public enum Type {
        FIREFOX,
        CHROME,
        IE
    }

}
