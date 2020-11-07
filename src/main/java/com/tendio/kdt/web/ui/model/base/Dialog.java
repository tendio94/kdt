package com.tendio.kdt.web.ui.model.base;

public class Dialog extends WebElementImpl {
    private static final String FIND_BY_NAME_XPATH = ".//div[@class='popupContent']" +
            "//span[contains(text(),'%s')]//ancestor::div[contains(@class, 'gwt-PopupPanel')]";
    private static final String FIND_ELEMENT_CONTAINER_XPATH = ".//div[contains(@class, 'gwt-PopupPanel')]";

    @Override
    public String getFindByNameXpath() {
        return FIND_BY_NAME_XPATH;
    }

    @Override
    public String getElementContainerXpath() {
        return FIND_ELEMENT_CONTAINER_XPATH;
    }
}
