package Selenium;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.bidi.LogInspector;
import org.openqa.selenium.bidi.log.ConsoleLogEntry;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.util.concurrent.*;

public class SeleniumTests {

    private WebDriver driver;

    @Before
    public void browser() {
        ChromeOptions options = new ChromeOptions();
        // webSocketUrl= true enables the Communication between the WebDriver with the browser
        options.setCapability("webSocketUrl", true);
        driver = new ChromeDriver(options);
        driver.get("https://www.trivago.co.uk");
    }

    @Test
    public void testJavaScriptConsoleLog() {
        CopyOnWriteArrayList<ConsoleLogEntry> logs = new CopyOnWriteArrayList<>();

        try (LogInspector logInspector = new LogInspector(driver)) {
            logInspector.onConsoleEntry(logs::add);
        } catch (Exception e) {
            // Handle exceptions
        }

        driver.findElement(By.id("consoleLog")).click();

    }

    @After
    public void closeBrowser() {
        driver.quit();
    }

}
