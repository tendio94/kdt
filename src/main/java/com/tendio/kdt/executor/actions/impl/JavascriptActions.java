package com.tendio.kdt.executor.actions.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.tendio.kdt.executor.actions.annotation.ActionClass;
import com.tendio.kdt.executor.actions.annotation.ActionDefinition;
import com.tendio.kdt.executor.actions.annotation.ActionDescription;
import com.tendio.kdt.executor.actions.impl.utils.JavascriptUtils;
import com.tendio.kdt.executor.browser.Browser;
import com.tendio.kdt.reporting.Report;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

@ActionClass
public class JavascriptActions {

    @ActionDescription("Scrolls view to the specified element")
    @ActionDefinition("Scroll to element by xpath=\"Xpath\"")
    public void scrollToElement(String xpath) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(xpath), "Xpath can`t be null or empty");

        WebElement element = Browser.getBrowser().findElement(By.xpath(xpath));
        JavascriptUtils.scrollIntoView(Browser.getBrowser(), element);
        Report.getReport().info("After scrolled to the element: " + element, Browser.getScreenshot());
    }

}
