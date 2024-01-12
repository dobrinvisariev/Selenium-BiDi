package Selenium;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.bidi.LogInspector;
import org.openqa.selenium.bidi.log.ConsoleLogEntry;
import org.openqa.selenium.bidi.log.JavascriptLogEntry;
import org.openqa.selenium.bidi.log.LogLevel;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SeleniumTests {

    private WebDriver driver;


    @Before
    public void browser() {
        ChromeOptions options = new ChromeOptions();
        // webSocketUrl= true enables the Communication between the WebDriver with the browser
        options.setCapability("webSocketUrl", true);
        driver = new ChromeDriver(options);

    }

    @Test
    public void testJavaScriptConsoleLog() {

        CopyOnWriteArrayList<ConsoleLogEntry> logsList = new CopyOnWriteArrayList<>();

        try (LogInspector logInspector = new LogInspector(driver)) {
            logInspector.onConsoleEntry(logsList::add);
        }

        driver.get("https://www.selenium.dev/selenium/web/bidi/logEntryAdded.html");
        driver.findElement(By.id("consoleLog")).click();
        driver.findElement(By.id("consoleError")).click();
        // wait until the logs list has some entry
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(_d -> !logsList.isEmpty());
        Assertions.assertEquals("Hello, world!", logsList.get(0).getText());

        int i = 0;
        for (ConsoleLogEntry log : logsList) {

            System.out.println(" TEST 1: LOG LIST " + i + ": " + log.getText() + "\n");
            i++;
        }
    }

    @Test
    public void testToListenToJavaScriptAndConsoleLog() throws InterruptedException, ExecutionException, TimeoutException {
        try (LogInspector logInspector = new LogInspector(driver)) {
            CompletableFuture<ConsoleLogEntry> consoleFutureResult = new CompletableFuture<>();
            CompletableFuture<JavascriptLogEntry> jsFutureResult = new CompletableFuture<>();
            //Sets up a listener upon a console log entry
            logInspector.onConsoleEntry(consoleFutureResult::complete);
            logInspector.onJavaScriptLog(jsFutureResult::complete);

            driver.get("https://www.selenium.dev/selenium/web/bidi/logEntryAdded.html");
            driver.findElement(By.id("consoleLog")).click();
            driver.findElement(By.id("jsException")).click();

            ConsoleLogEntry logsList = consoleFutureResult.get(10, TimeUnit.SECONDS);
            JavascriptLogEntry jsLogEntry = jsFutureResult.get(10, TimeUnit.SECONDS);

            System.out.println(" Console log entry: " + logsList.getText() + "\n");
            System.out.println(" JavaScript log entry: " + jsLogEntry.getText() + "\n");

            //console log entry assertions
            Assertions.assertEquals("Hello, world!", logsList.getText());
            Assertions.assertNull(logsList.getRealm());
            Assertions.assertEquals(1, logsList.getArgs().size());
            Assertions.assertEquals("console", logsList.getType());
            Assertions.assertEquals("log", logsList.getMethod());

            System.out.println(" Reached middle of test 2" + "\n");
            //Assertions.assertNull(logsList.getStackTrace());

            //JavaScript log entry assertions
            Assertions.assertEquals("Error: Not working", jsLogEntry.getText());
            Assertions.assertEquals("javascript", jsLogEntry.getType());
            Assertions.assertEquals(LogLevel.ERROR, jsLogEntry.getLevel());

            System.out.println(" Reached End of TEST 2 - Not suspicious any more! PARTYYY! " + "\n");

        }
    }

    @After
    public void closeBrowser() {
        driver.quit();
    }

}
