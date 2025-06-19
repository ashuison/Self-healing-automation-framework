package com.ashutosh.framework.utils;

import com.ashutosh.framework.locators.LocatorType;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class SmartElementFinder {

    private WebDriver driver;
    private WebDriverWait wait;
    private RetryAnalyzer retryAnalyzer;

    // Fallback strategy metrics
    private int fallbackSuccessCount = 0;
    private int totalElementFinds = 0;

    public SmartElementFinder(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        this.retryAnalyzer = new RetryAnalyzer();
    }

    /**
     * Find element with retry mechanism and multiple locator strategies
     */
    public WebElement findElement(String locatorValue) {
        totalElementFinds++;
        return retryAnalyzer.retry(() -> findElementWithStrategies(locatorValue));
    }

    /**
     * Find element with custom retry settings
     */
    public WebElement findElement(String locatorValue, int maxRetries, int delaySeconds) {
        totalElementFinds++;
        return retryAnalyzer.retry(() -> findElementWithStrategies(locatorValue), maxRetries, delaySeconds);
    }

    /**
     * Find element using multiple locator strategies (FALLBACK LOGIC)
     */
    private WebElement findElementWithStrategies(String locatorValue) {
        List<LocatorType> strategies = Arrays.asList(
                LocatorType.XPATH,
                LocatorType.CSS,
                LocatorType.TEXT,
                LocatorType.PARTIAL_TEXT);

        for (int i = 0; i < strategies.size(); i++) {
            LocatorType strategy = strategies.get(i);
            try {
                WebElement element = findElementByStrategy(locatorValue, strategy);
                if (element != null && isElementVisible(element)) {
                    if (i > 0) {
                        fallbackSuccessCount++;
                        System.out.println("Fallback strategy " + strategy + " succeeded for: " + locatorValue);
                    } else {
                        System.out.println("Primary strategy " + strategy + " succeeded for: " + locatorValue);
                    }
                    return element;
                }
            } catch (NoSuchElementException | TimeoutException e) {
                System.out.println("Strategy " + strategy + " failed for locator: " + locatorValue);
            }
        }

        throw new NoSuchElementException("Element not found using any strategy: " + locatorValue);
    }

    /**
     * Find element by specific strategy
     */
    private WebElement findElementByStrategy(String locatorValue, LocatorType strategy) {
        By by = getByLocator(locatorValue, strategy);
        return wait.until(ExpectedConditions.elementToBeClickable(by));
    }

    /**
     * Get By locator based on strategy
     */
    private By getByLocator(String locatorValue, LocatorType strategy) {
        switch (strategy) {
            case XPATH:
                return By.xpath(locatorValue);
            case CSS:
                return By.cssSelector(locatorValue);
            case TEXT:
                return By.xpath("//*[text()='" + locatorValue + "']");
            case PARTIAL_TEXT:
                return By.xpath("//*[contains(text(),'" + locatorValue + "')]");
            default:
                throw new IllegalArgumentException("Unsupported locator strategy: " + strategy);
        }
    }

    /**
     * Check if element is visible and clickable
     */
    private boolean isElementVisible(WebElement element) {
        try {
            return element.isDisplayed() && element.isEnabled();
        } catch (StaleElementReferenceException e) {
            return false;
        }
    }

    /**
     * Find element and click with retry mechanism
     */
    public void findAndClick(String locatorValue) {
        retryAnalyzer.retry(() -> {
            WebElement element = findElement(locatorValue);
            element.click();
        });
    }

    /**
     * Find element and click with custom retry settings
     */
    public void findAndClick(String locatorValue, int maxRetries, int delaySeconds) {
        retryAnalyzer.retry(() -> {
            WebElement element = findElement(locatorValue);
            element.click();
        }, maxRetries, delaySeconds);
    }

    /**
     * Find element and send keys with retry mechanism
     */
    public void findAndSendKeys(String locatorValue, String text) {
        retryAnalyzer.retry(() -> {
            WebElement element = findElement(locatorValue);
            element.clear();
            element.sendKeys(text);
        });
    }

    /**
     * Find element and send keys with custom retry settings
     */
    public void findAndSendKeys(String locatorValue, String text, int maxRetries, int delaySeconds) {
        retryAnalyzer.retry(() -> {
            WebElement element = findElement(locatorValue);
            element.clear();
            element.sendKeys(text);
        }, maxRetries, delaySeconds);
    }

    /**
     * Find element and get text with retry mechanism
     */
    public String findAndGetText(String locatorValue) {
        return retryAnalyzer.retry(() -> {
            WebElement element = findElement(locatorValue);
            return element.getText();
        });
    }

    /**
     * Find element and get text with custom retry settings
     */
    public String findAndGetText(String locatorValue, int maxRetries, int delaySeconds) {
        return retryAnalyzer.retry(() -> {
            WebElement element = findElement(locatorValue);
            return element.getText();
        }, maxRetries, delaySeconds);
    }

    /**
     * Check if element exists with retry
     */
    public boolean elementExists(String locatorValue) {
        try {
            findElement(locatorValue);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Wait for element to be present with retry
     */
    public WebElement waitForElement(String locatorValue) {
        return retryAnalyzer.retry(() -> findElementWithStrategies(locatorValue));
    }

    /**
     * Wait for element to be present with custom retry settings
     */
    public WebElement waitForElement(String locatorValue, int maxRetries, int delaySeconds) {
        return retryAnalyzer.retry(() -> findElementWithStrategies(locatorValue), maxRetries, delaySeconds);
    }

    // ========== FALLBACK STRATEGY METRICS ==========

    /**
     * Get fallback strategy success rate percentage
     */
    public double getFallbackSuccessRate() {
        return totalElementFinds > 0 ? (double) fallbackSuccessCount / totalElementFinds * 100 : 0;
    }

    /**
     * Get total number of fallback strategy successes
     */
    public int getFallbackSuccessCount() {
        return fallbackSuccessCount;
    }

    /**
     * Get total number of element find operations
     */
    public int getTotalElementFinds() {
        return totalElementFinds;
    }

    /**
     * Print fallback strategy metrics
     */
    public void printFallbackMetrics() {
        System.out.println("=== Fallback Strategy Metrics ===");
        System.out.println("Total element finds: " + totalElementFinds);
        System.out.println("Fallback strategy successes: " + fallbackSuccessCount);
        System.out.println("Fallback success rate: " + String.format("%.2f", getFallbackSuccessRate()) + "%");
        System.out.println("==================================");
    }

    /**
     * Print all metrics (both retry and fallback)
     */
    public void printAllMetrics() {
        retryAnalyzer.printMetrics();
        printFallbackMetrics();
    }

    /**
     * Reset all metrics
     */
    public void resetMetrics() {
        retryAnalyzer.resetMetrics();
        fallbackSuccessCount = 0;
        totalElementFinds = 0;
    }
}
