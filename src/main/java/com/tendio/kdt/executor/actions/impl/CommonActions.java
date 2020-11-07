package com.tendio.kdt.executor.actions.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.tendio.kdt.TestProperties;
import com.tendio.kdt.configurator.model.TestSuiteParameters;
import com.tendio.kdt.executor.actions.annotation.ActionClass;
import com.tendio.kdt.executor.actions.annotation.ActionDefinition;
import com.tendio.kdt.executor.actions.impl.utils.JavascriptUtils;
import com.tendio.kdt.executor.actions.model.ActionFactory;
import com.tendio.kdt.executor.browser.Browser;
import com.tendio.kdt.executor.exception.InterruptedTestCaseException;
import com.tendio.kdt.reporting.Report;
import com.tendio.kdt.reporting.html.LogLevel;
import com.tendio.kdt.web.ui.model.base.Button;
import com.tendio.kdt.web.ui.model.base.Link;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

@ActionClass
public class CommonActions {
    protected static final Logger LOGGER = LogManager.getLogger();
    private static final String PAIRS_SEPARATOR = ",";
    private static final String KEY_VALUE_SEPARATOR = "=";
    private static final String IS_LOGGED_IN_XPATH = ".//*[contains(text(), 'Logged in as')]";

    //required for methods invocation using reflection
    public CommonActions() {
    }

    //parses Key-Value map from String of a given form: "Key=Value"
    static Map<String, String> parseKeyValueString(@Nonnull String keysMap) {
        String parameterizedKeysMap = ActionFactory.resolveTestSuiteParameters(keysMap);
        Map<String, String> keyValueMap = Maps.newLinkedHashMap();
        String[] pairs = parameterizedKeysMap.split(PAIRS_SEPARATOR);

        Arrays.asList(pairs).forEach(s -> {
            {
                String[] pair = s.split(KEY_VALUE_SEPARATOR);
                String key = pair[0];
                String value = pair[1];
                keyValueMap.put(key, value);
            }
        });

        return keyValueMap;
    }

    private static boolean isLoggedIn() {
        try {
            return Browser.getBrowser().findElement(By.xpath(IS_LOGGED_IN_XPATH)).isDisplayed();
        } catch (NoSuchElementException ignored) {
            return false;
        }
    }

    private static boolean isAlertPresent() {
        Wait<WebDriver> wait = new FluentWait<>(Browser.getBrowser()).withTimeout(Duration.ofSeconds(1));
        try {
            return wait.until(ExpectedConditions.alertIsPresent()) != null;
        } catch (TimeoutException ignored) {
        }
        return false;
    }

    public static void logIn(String username, String password) {
        WebElement userInput = Browser.getBrowser().findElement(By.name("username"));
        userInput.clear();
        userInput.sendKeys(username);

        WebElement passwordInput = Browser.getBrowser().findElement(By.name("password"));
        passwordInput.clear();
        passwordInput.sendKeys(password);

        Browser.get(Button.class, null, "Login", 2).click();
    }

    public static void acceptAlertWithReporting() {
        if (isAlertPresent()) {
            Alert alert = Browser.getBrowser().switchTo().alert();
            String alertText = alert.getText();
            alert.accept();
            Report.getReport().warn("After accepting unexpected alert present with text: " + alertText);
        }
    }

    @ActionDefinition("Generate random string of length \"Length\" and store to parameter \"Parameter\"")
    public final void generateRandomString(String stringLength, String parameter) {
        int length = Integer.parseInt(stringLength);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(parameter), "Parameter name can`t be null or empty");
        Preconditions.checkArgument(length > 0, "Random string length must be > 0");

        String randomString = new RandomString(length).getValue();
        TestSuiteParameters.addParameter(parameter, randomString);

        String message = String.format("Successfully stored random string of length %s: %s=%s", length, parameter, randomString);
        Report.getReport().info(message);
    }

    @ActionDefinition("Move to element by xpath=\"xpath\"")
    public void moveToElement(String xpath) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(xpath), "Xpath can`t be null or empty");
        WebElement element = Browser.getBrowser().findElement(By.xpath(xpath));
        new Actions(Browser.getBrowser()).moveToElement(element).build().perform();

        String message = String.format("Moved to element by xpath: %s", xpath);
        Report.getReport().info(message, Browser.getScreenshot());
    }

    @ActionDefinition("Send keys \"Text\" by xpath=\"xpath\"")
    public void sendKeysByXpath(String text, String xpath) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(xpath), "Xpath can`t be null or empty");
        Browser.getBrowser().findElement(By.xpath(xpath)).sendKeys(text);

        String message = String.format("Send keys %s by xpath: %s", text, xpath);
        Report.getReport().info(message, Browser.getScreenshot());
    }

    @ActionDefinition("Navigate to \"TabName\" tab")
    public void navigateToTab(String tabName) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(tabName), "Tab name can`t be null or empty");
        Browser.get(Link.class, null, tabName).click();

        String message = String.format("Successfully navigated to tab '%s'", tabName);
        Report.getReport().info(message, Browser.getScreenshot());
    }

    @ActionDefinition("Login as user \"user\" with password \"password\"")
    public final void login(String username, String password) {
        Browser.initBrowser();

        String property = "test.server";
        String validationMessage = property + " property is not specified in test.properties file";
        String server = Objects.requireNonNull(TestProperties.getProperty("test.server"), validationMessage);
        if (!Browser.getBrowser().getCurrentUrl().contains(server)) {
            Browser.getBrowser().get(server);
        }

        String message = String.format("Logged in as user %s with password %s", username, password);
        if (!isLoggedIn()) {
            logIn(username, password);
        } else {
            message = "Already logged in";
        }
        if (isLoggedIn()) {
            Report.getReport().info(message, Browser.getScreenshot());
        } else {
            message = String.format("Couldn`t login as user %s with password %s", username, password);
            throw new InterruptedTestCaseException(message);
        }
    }

    @ActionDefinition("Verify element with xpath=\"Xpath\" \"isClickable, is Displayed, isSelected\"=\"true/false\"")
    public void verifyElementState(String xpath, String methodName, String expectedState) {
        boolean expected = Boolean.valueOf(expectedState);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(methodName),
                "Element`s expected state can`t be null or empty");

        RemoteWebElement element = (RemoteWebElement) Browser.getBrowser().findElement(By.xpath(xpath));
        Object returned = null;
        try {
            Method method = element.getClass().getMethod(methodName);
            returned = method.invoke(element);
        } catch (ReflectiveOperationException e) {
            String message = "Could not invoke method: " + methodName + ". Cause: " + e.getMessage();
            Report.getReport().error(message, Browser.getScreenshot());
        }

        if (returned instanceof Boolean) {
            String message;
            LogLevel logLevel;
            if ((Boolean) returned == expected) {
                logLevel = LogLevel.INFO;
                message = String.format("Element with xpath='%s' %s=%s", xpath, methodName, returned);
            } else {
                logLevel = LogLevel.ERROR;
                message = String.format("Element with xpath='%s' %s: AR=%s; ER=%s", xpath, methodName, returned, expected);
            }
            Report.getReport().log("Verify element has expected state", message, Browser.getScreenshot(), logLevel);
        }
    }

    @ActionDefinition("Navigate to menu \"Limits>Dashboard\"")
    public void navigateToMenu(String menusSequence) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(menusSequence), "Menu sequence can`t be null or empty!");
        final String separator = ">";
        String[] menuItems = menusSequence.split(separator);

        navigateToTab(menuItems[0]);
        for (int i = 1; i < menuItems.length; i++) {
            String xpath = String.format(".//div[contains(@class,'flz_scrollpanel')]//a[text()='%s']", menuItems[i]);
            Browser.getBrowser().findElement(By.xpath(xpath)).click();
        }

        String message = "Navigated to menu items chain: " + menusSequence;
        Report.getReport().info(message, Browser.getScreenshot());
    }


    @ActionDefinition("Open URL \"url\"")
    public final void openUrl(String url) {
        Browser.getBrowser().get(url);

        String message = String.format("Url '%s' has been opened!", url);
        Report.getReport().info(message, Browser.getScreenshot());
    }

    @ActionDefinition("Click by xpath=\"xpath\"")
    public final void clickByXpath(String xpath) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(xpath), "Xpath can`t be null or empty");

        WebElement element = Browser.getBrowser().findElement(By.xpath(xpath));
        JavascriptUtils.scrollIntoView(Browser.getBrowser(), element);
        element.click();
        //acceptAlertWithReporting();

        String message = String.format("Clicked by xpath: %s", xpath);
        Report.getReport().info(message, Browser.getScreenshot());
    }

    @ActionDefinition("Click the \"ButtonName\" button")
    public void clickButton(String buttonName) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(buttonName), "Button name can`t be null or empty");

        Button button = Browser.get(Button.class, null, buttonName);
        button.click();

        String message = String.format("Successfully clicked on the button '%s'", buttonName);
        Report.getReport().info(message, Browser.getScreenshot());

    }

    public static class RandomString {
        private static final String CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        private SecureRandom rnd;
        private String value;

        RandomString(int length) {
            this.rnd = new SecureRandom();
            this.value = generate(length);
        }

        public String getValue() {
            return value;
        }

        private String generate(int length) {
            StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                sb.append(CHARACTERS.charAt(rnd.nextInt(CHARACTERS.length())));
            }
            return sb.toString();
        }
    }
}
