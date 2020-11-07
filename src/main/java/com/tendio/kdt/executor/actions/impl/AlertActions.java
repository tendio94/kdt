package com.tendio.kdt.executor.actions.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.tendio.kdt.executor.actions.annotation.ActionClass;
import com.tendio.kdt.executor.actions.annotation.ActionDefinition;
import com.tendio.kdt.executor.browser.Browser;
import com.tendio.kdt.executor.exception.InterruptedTestCaseException;
import com.tendio.kdt.reporting.Report;
import org.openqa.selenium.Alert;

import java.util.Arrays;

@ActionClass
public final class AlertActions extends CommonActions {

    @ActionDefinition("Handle alert: \"GETTEXT, DISMISS, ACCEPT\"")
    public void handleAlert(String action) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(action), "Action can`t be null or empty!");

        AlertAction alertAction;
        try {
            alertAction = AlertAction.valueOf(action);
        } catch (IllegalArgumentException e) {
            String message = String.format("Unsupported action: %s. Supported actions: %s", action, Arrays.asList(AlertAction.values()));
            throw new InterruptedTestCaseException(message, e);
        }

        Alert alert = Browser.getBrowser().switchTo().alert();
        switch (alertAction) {
            case DISMISS:
                Report.getReport().info("Before alert is dismissed", Browser.getScreenshot());
                alert.dismiss();
                Report.getReport().info("Alert has been dismissed", Browser.getScreenshot());
                break;
            case ACCEPT:
                Report.getReport().info("Before alert is accepted", Browser.getScreenshot());
                alert.accept();
                Report.getReport().info("Alert has been accepted", Browser.getScreenshot());
                break;
            default:
                Report.getReport().info("Alert text: " + alert.getText(), Browser.getScreenshot());
        }
    }

    public enum AlertAction {
        DISMISS, ACCEPT, GETTEXT
    }
}
