package com.ashutosh.framework.tests;

import com.ashutosh.framework.base.BaseTest;
import com.ashutosh.framework.utils.SmartElementFinder;
import org.testng.annotations.Test;

public class SampleTest extends BaseTest {

    @Test
    public void testSelfHealingLogin() {
        // Navigate to test page
        navigateTo("https://the-internet.herokuapp.com/login");

        SmartElementFinder finder = new SmartElementFinder(driver);

        // Test self-healing element finding with default retry (3 attempts, 2 seconds
        // delay)
        finder.findAndSendKeys("//input[@id='username']", "tomsmith");
        finder.findAndSendKeys("//input[@id='password']", "SuperSecretPassword!");

        // This will fail with normal locator but work with TEXT fallback
        finder.findAndClick("//button[@id='invalid']");

        // Verify successful login
        String successMessage = finder.findAndGetText("//div[@class='flash success']");
        assert successMessage.contains("You logged into a secure area");

        // Print all metrics (both retry and fallback)
        finder.printAllMetrics();
    }

    @Test
    public void testElementRetryMechanism() {
        navigateTo("https://the-internet.herokuapp.com/dynamic_loading/1");

        SmartElementFinder finder = new SmartElementFinder(driver);

        // Click start button
        finder.findAndClick("//button[contains(text(),'Start')]");

        // Wait for element to appear with retry mechanism (default: 3 attempts, 2
        // seconds)
        String hiddenText = finder.findAndGetText("//h4[contains(text(),'Hello World')]");
        assert hiddenText.equals("Hello World!");

        // Print retry metrics only
        finder.printFallbackMetrics();
    }

    @Test
    public void testCustomRetrySettings() {
        navigateTo("https://the-internet.herokuapp.com/dynamic_loading/2");

        SmartElementFinder finder = new SmartElementFinder(driver);

        // Click start button
        finder.findAndClick("//button[contains(text(),'Start')]");

        // Wait for element with custom retry settings (5 attempts, 1 second delay)
        String hiddenText = finder.findAndGetText("//h4[contains(text(),'Hello World')]", 5, 1);
        assert hiddenText.equals("Hello World!");
    }

    @Test
    public void testMultipleLocatorStrategies() {
        navigateTo("https://the-internet.herokuapp.com/");

        SmartElementFinder finder = new SmartElementFinder(driver);

        // Test different locator strategies with retry
        assert finder.elementExists("//h1[contains(text(),'Welcome')]");
        assert finder.elementExists("h1.heading");
        assert finder.elementExists("Welcome to the-internet");

        // Print fallback strategy metrics
        finder.printFallbackMetrics();
    }

    @Test
    public void testFallbackLocatorStrategy() {
        navigateTo("https://the-internet.herokuapp.com/checkboxes");

        SmartElementFinder finder = new SmartElementFinder(driver);

        // This will use fallback strategies if the primary locator fails
        finder.findAndClick("//input[@type='checkbox'][1]");

        // Verify checkbox is checked
        String checkboxAttribute = finder.findElement("//input[@type='checkbox'][1]").getAttribute("checked");
        assert checkboxAttribute != null;
    }

    @Test
    public void testWaitForElementWithRetry() {
        navigateTo("https://the-internet.herokuapp.com/dynamic_controls");

        SmartElementFinder finder = new SmartElementFinder(driver);

        // Click enable button
        finder.findAndClick("//button[contains(text(),'Enable')]");

        // Wait for input field to be enabled with retry
        finder.waitForElement("//input[@type='text']");

        // Verify input is enabled
        boolean isEnabled = finder.findElement("//input[@type='text']").isEnabled();
        assert isEnabled;

        // Print comprehensive metrics
        finder.printAllMetrics();
    }
}
