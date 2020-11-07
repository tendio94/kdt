package com.tendio.kdt.executor.actions.impl.utils;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public final class JavascriptUtils {
    private static final String STYLE_ATTRIBUTE_VALUE_SEPARATOR = ":";
    private JavascriptUtils() {
    }

    public static void highlightElement(WebDriver driver, WebElement element) {
        if (driver instanceof JavascriptExecutor) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].setAttribute('style', 'background: yellow; border: 2px solid red;');", element);
        }
    }

    public static void removeHighlight(WebDriver driver, WebElement element) {
        if (driver instanceof JavascriptExecutor) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].setAttribute('style', '');", element);
        }
    }

    public static void setStyleCssAttribute(WebDriver driver, WebElement element, String attribute, String value) {
        if (driver instanceof JavascriptExecutor) {
            String style = element.getAttribute("style") + attribute + STYLE_ATTRIBUTE_VALUE_SEPARATOR + value + ";";
            String script = String.format("arguments[0].setAttribute('style', '%s');", style);
            ((JavascriptExecutor) driver).executeScript(script, element);
        }
    }

    //currently not working properly
/*    public static void scrollWindow(WebDriver driver, int x, int y) {
        if (driver instanceof JavascriptExecutor) {
            String script = String.format("window.scrollBy(%s, %s), \"\"", x, y);
            ((JavascriptExecutor) driver).executeScript(script);
        }
    }*/

    public static void scrollIntoView(WebDriver driver, WebElement element) {
        if (driver instanceof JavascriptExecutor) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true)", element);
        }
    }
}
