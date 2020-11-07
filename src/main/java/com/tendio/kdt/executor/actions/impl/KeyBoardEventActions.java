package com.tendio.kdt.executor.actions.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.tendio.kdt.executor.actions.annotation.ActionClass;
import com.tendio.kdt.executor.actions.annotation.ActionDefinition;
import com.tendio.kdt.executor.browser.Browser;
import com.tendio.kdt.executor.exception.InterruptedTestCaseException;
import com.tendio.kdt.reporting.Report;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;

import java.util.Arrays;

@ActionClass
public class KeyBoardEventActions extends CommonActions {

    @ActionDefinition("Press enter key")
    public void pressEnter() {
        new Actions(Browser.getBrowser()).sendKeys(Keys.ENTER).build().perform();
        Report.getReport().info("After ENTER key is released", Browser.getScreenshot());
    }

    @ActionDefinition("Press keyboard key: \"Key\"")
    public void pressKeyboardKey(String key) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(key), "Action can`t be null or empty!");
        Actions action = new Actions(Browser.getBrowser());

        Keys _key;
        try {
            _key = Keys.valueOf(key);
        } catch (IllegalArgumentException e) {
            String message = String.format("Unsupported key: %s. Supported keys: %s", action, Arrays.asList(Keys.values()));
            throw new InterruptedTestCaseException(message, e);
        }

        action.keyUp(_key).build().perform();
        Report.getReport().info("After key " + _key + " is pressed", Browser.getScreenshot());
        action.keyDown(_key).build().perform();
        Report.getReport().info("After key " + _key + " is released", Browser.getScreenshot());
    }
}
