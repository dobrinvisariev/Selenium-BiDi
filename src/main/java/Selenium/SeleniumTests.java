package Selenium;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.bidi.LogInspector;
import org.openqa.selenium.bidi.log.ConsoleLogEntry;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.concurrent.CopyOnWriteArrayList;

public class SeleniumTests {

    private WebDriver driver;
    CopyOnWriteArrayList<ConsoleLogEntry> logsList = new CopyOnWriteArrayList<>();

    @Before
    public void browser() {
        ChromeOptions options = new ChromeOptions();
        // webSocketUrl= true enables the Communication between the WebDriver with the browser
        options.setCapability("webSocketUrl", true);
        driver = new ChromeDriver(options);

    }

    @Test
    public void testJavaScriptConsoleLog() {

        try (LogInspector logInspector = new LogInspector(driver)) {
            logInspector.onConsoleEntry(logsList::add);
        }

        driver.get("https://www.selenium.dev/selenium/web/bidi/logEntryAdded.html");
        driver.findElement(By.id("consoleLog")).click();
        driver.findElement(By.id("consoleError")).click();
        // wait until the logs list has some entry
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(_d -> !logsList.isEmpty());
        Assertions.assertEquals("Hello, world!", logsList.get(0).getText());
        showConsoleLog();

    }

    public void showConsoleLog() {

        if (!logsList.isEmpty()) {
            for (ConsoleLogEntry log : logsList) {
                System.out.println(" Console logs: " + log.getText());
            }
        } else {
            System.out.println(" No logs found! ");
        }
    }

    @After
    public void closeBrowser() {
        driver.quit();
    }

}
