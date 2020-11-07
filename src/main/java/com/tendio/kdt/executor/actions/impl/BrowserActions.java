package com.tendio.kdt.executor.actions.impl;

import com.google.common.base.Preconditions;
import com.tendio.kdt.executor.actions.annotation.ActionClass;
import com.tendio.kdt.executor.actions.annotation.ActionDefinition;
import com.tendio.kdt.executor.browser.Browser;
import com.tendio.kdt.reporting.Report;

@ActionClass
public class BrowserActions extends CommonActions {

    @ActionDefinition("Refresh page")
    public void regreshPage() {
        regreshPageWithWaiter("1");
    }

    @ActionDefinition("Refresh page with waiting \"Seconds\" afterwards")
    public void regreshPageWithWaiter(String secondsString) {
        int seconds = Integer.parseInt(secondsString);
        Preconditions.checkArgument(seconds > 0, "Timeout value must be > 0");
        Browser.getBrowser().navigate().refresh();
        Browser.applyWaiter(seconds);
        Report.getReport().info("After page has been refreshed", Browser.getScreenshot());
    }
}
