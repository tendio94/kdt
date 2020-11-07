package com.tendio.kdt.web.ui.model.base;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsElement;
import org.openqa.selenium.interactions.Locatable;

public interface CustomWebElement extends WebElement, WrapsElement, Locatable {

    /**
     * @return xpath string to find particular element by it`s name or label depending on markup
     */
    String getFindByNameXpath();

    /**
     * @return xpath string to find all elements of the class
     */
    String getElementContainerXpath();

    void setDriver(WebDriver driver);

    void setElement(WebElement element);

}
