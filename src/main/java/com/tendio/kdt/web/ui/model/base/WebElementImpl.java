package com.tendio.kdt.web.ui.model.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Locatable;
import org.openqa.selenium.interactions.internal.Coordinates;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static com.tendio.kdt.executor.browser.Browser.getBrowser;

//added implementation of deprecated Locatable interface
//to use api of org.openqa.selenium.interactions.Actions
public abstract class WebElementImpl implements CustomWebElement, org.openqa.selenium.internal.Locatable {
    protected static final Logger LOGGER = LogManager.getLogger();
    protected WebDriver driver;
    protected WebElement element;

    protected WebElementImpl() {
    }

    public void setElement(WebElement element) {
        this.element = element;
    }

    public WebDriver getDriver() {
        return driver;
    }

    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }

    @Override
    public void click() {
        element.click();
    }

    @Override
    public void submit() {
        element.submit();
    }

    @Override
    public void sendKeys(CharSequence... keysToSend) {
        element.sendKeys(keysToSend);
    }

    @Override
    public void clear() {
        element.clear();
    }

    @Override
    public String getTagName() {
        return element.getTagName();
    }

    @Override
    public String getAttribute(String name) {
        return element.getAttribute(name);
    }

    @Override
    public boolean isSelected() {
        return element.isSelected();
    }

    @Override
    public boolean isEnabled() {
        return element.isEnabled();
    }

    @Override
    public String getText() {
        return element.getText();
    }

    @Override
    public List<WebElement> findElements(By by) {
        return element.findElements(by);
    }

    @Override
    public WebElement findElement(By by) {
        return element.findElement(by);
    }

    @Override
    public boolean isDisplayed() {
        return element.isDisplayed();
    }

    @Override
    public Point getLocation() {
        return element.getLocation();
    }

    @Override
    public Dimension getSize() {
        return element.getSize();
    }

    @Override
    public Rectangle getRect() {
        return element.getRect();
    }

    @Override
    public String getCssValue(String propertyName) {
        return element.getCssValue(propertyName);
    }

    @Override
    public <X> X getScreenshotAs(OutputType<X> target) throws WebDriverException {
        return element.getScreenshotAs(target);
    }

    @Override
    public WebElement getWrappedElement() {
        return element;
    }

    @Override
    public Coordinates getCoordinates() {
        return ((Locatable) element).getCoordinates();
    }

    public static class Constructor {

        public static <T extends CustomWebElement> T construct(Class<T> clazz, WebDriver driver, WebElement element) {
            T instance = getNewInstanceViaDefaultConstructor(clazz);
            instance.setElement(element);
            instance.setDriver(driver);
            return instance;
        }

        public static <T extends CustomWebElement> T getElementByLocatorsInsideParent(T instance, @Nullable SearchContext parent,
                                                                                      int number, @Nullable String textLocator) {
            String xpath = textLocator == null ? instance.getElementContainerXpath()
                    : String.format(instance.getFindByNameXpath(), textLocator);
            SearchContext searchContext = (parent != null) ? parent : getBrowser();
            List<WebElement> elements = searchContext.findElements(By.xpath(xpath));
            if (elements.isEmpty()) {
                String message = String.format("Couldn`t find elements by xpath: %s", xpath);
                throw new NoSuchElementException(message);
            }

            WebElement webElement = getWebElementFromList(elements, number);
            instance.setElement(webElement);
            return instance;
        }

        public static <T extends CustomWebElement> T getNewInstanceViaDefaultConstructor(Class<T> clazz) {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                String message = String.format("Couldn`t instantiate web element of class %s : %s", clazz, e.getMessage());
                throw new RuntimeException(message);
            }
        }

        private static WebElement getWebElementFromList(List<WebElement> elements, int number) {
            int elementsSize = elements.size();
            if (number >= elementsSize + 1) {
                String message = String.format("Couldn`t find element with number=%s: elements={%s}, total=%s",
                        number, elements, elementsSize);
                throw new NoSuchElementException(message);
            }
            return elements.get(number - 1);
        }
    }

    public static class MixedLocator {
        public static final String MIXED_LOCATOR_SEPARATOR = "|";
        public static final String ESCPAED_MIXED_LOCATOR_SEPARATOR = "\\|";
        public static final String NUMBER_LOCATOR_IDENTIFIER = "number=";
        private int number;
        private String name;

        public MixedLocator(String name, int number) {
            this.number = number;
            this.name = name;
        }

        // mixed = contains both name and number locator separated by \n
        public static boolean isMixedLocator(String locator) {
            return (locator != null) && locator.contains(MIXED_LOCATOR_SEPARATOR);
        }

        public static MixedLocator fromString(String locator) {
            String[] splitLocator = locator.split(ESCPAED_MIXED_LOCATOR_SEPARATOR);
            String name = splitLocator[0];
            int number = Integer.parseInt(removeNumberLocatorIdentifier(splitLocator[1]));
            return new MixedLocator(name, number);
        }

        private static String removeNumberLocatorIdentifier(String locator) {
            return locator.replace(NUMBER_LOCATOR_IDENTIFIER, "");
        }

        public int getNumber() {
            return number;
        }

        public String getName() {
            return name;
        }
    }

}
