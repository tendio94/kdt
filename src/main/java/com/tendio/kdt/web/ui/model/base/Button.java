package com.tendio.kdt.web.ui.model.base;

public class Button extends WebElementImpl {
    //using relative indexing because String.format() method is called only once
    private static final String FIND_BY_NAME_XPATH = ".//button//span[text()='%s'] " +
            "| //a[@title='%<s'] " +
            //horrible xpath for customized buttons with icons in HGCC
            "| .//a[contains(@class, 'gwt-Anchor') and text()='%<s']";
    private static final String FIND_ELEMENT_CONTAINER_XPATH = ".//button | .//input[@type='button']";

    @Override
    public String getFindByNameXpath() {
        return FIND_BY_NAME_XPATH;
    }

    @Override
    public String getElementContainerXpath() {
        return FIND_ELEMENT_CONTAINER_XPATH;
    }
}
