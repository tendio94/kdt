package com.tendio.kdt.executor.actions.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.tendio.kdt.executor.actions.annotation.ActionDefinition;
import com.tendio.kdt.executor.browser.Browser;
import com.tendio.kdt.reporting.Report;

public class WaiterActions extends CommonActions {

    @ActionDefinition("Wait for \"TimeAmount(in seconds)\" seconds")
    public void wait(String secondsString) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(secondsString), "Seconds value can`t be null or empty");
        int seconds = Integer.parseInt(secondsString);
        Preconditions.checkArgument(seconds >= 0, "Seconds value must be >= 0");

        Browser.applyWaiter(seconds);
        String message = String.format("After %s seconds have passed", secondsString);
        Report.getReport().info(message, Browser.getScreenshot());
    }
}
