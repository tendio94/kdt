package com.tendio.kdt.web.ui.model.base;

public class Link extends WebElementImpl {
    private static final String FIND_BY_NAME_XPATH = ".//a[text()='%s'] | .//a[contains(@title,'%<s')]";
    private static final String FIND_ELEMENT_CONTAINER_XPATH = ".//a";

    @Override
    public String getFindByNameXpath() {
        return FIND_BY_NAME_XPATH;
    }

    @Override
    public String getElementContainerXpath() {
        return FIND_ELEMENT_CONTAINER_XPATH;
    }
}
