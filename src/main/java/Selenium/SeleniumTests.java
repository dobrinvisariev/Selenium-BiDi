package Selenium;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.bidi.LogInspector;
import org.openqa.selenium.bidi.log.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
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
        // Enable the Communication between the WebDriver with the browser
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

        int i = 1;
        for (ConsoleLogEntry log : logsList) {

            System.out.println(" TEST 1: LOG LIST " + i + ": " + log.getText() + "\n");
            i++;
        }
    }

    @Test
    public void testToListenToJSExceptionAndConsoleLog() throws InterruptedException, ExecutionException, TimeoutException {
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
            JavascriptLogEntry jsExceptionList = jsFutureResult.get(10, TimeUnit.SECONDS);

            System.out.println(" Console log entry: " + logsList.getText() + "\n");
            System.out.println(" JavaScript log entry: " + jsExceptionList.getText() + "\n");

            //console log entry assertions
            Assertions.assertEquals("Hello, world!", logsList.getText());
            Assertions.assertNull(logsList.getRealm());
            Assertions.assertEquals(1, logsList.getArgs().size());
            Assertions.assertEquals("console", logsList.getType());
            Assertions.assertEquals("log", logsList.getMethod());

            System.out.println(" TEST 2: Console log entry assertions passed! " + "\n");
            //Assertions.assertNull(logsList.getStackTrace());

            //JavaScript log entry assertions
            Assertions.assertEquals("Error: Not working", jsExceptionList.getText());
            Assertions.assertEquals("javascript", jsExceptionList.getType());
            Assertions.assertEquals(LogLevel.ERROR, jsExceptionList.getLevel());

            System.out.println(" TEST 2: JavaScript log entry assertions passed! End of the test! " + "\n");
        }
    }

    // Test does not work
    public void jsExceptionLogger2ndTest() {

        List<JavascriptException> jsExceptionList = new ArrayList<>();
        // Cast WebDriver to ChromeDriver to access getDevTools
        ChromeDriver chromeDriver = (ChromeDriver) driver;
        DevTools devTools = chromeDriver.getDevTools();
        devTools.getDomains().events().addJavascriptExceptionListener(jsExceptionList::add);
        driver.get("https://www.selenium.dev/selenium/web/bidi/logEntryAdded.html");
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(_d -> !jsExceptionList.isEmpty());

        System.out.println(" Test 3 is complete. The js Exception list is: " + jsExceptionList + "\n");
    }

    @Test
    public void stackTraceForALog() throws TimeoutException, ExecutionException, InterruptedException {
        try (LogInspector logInspector = new LogInspector(driver)) {
            CompletableFuture<JavascriptLogEntry> jsFuture = new CompletableFuture<>();
            logInspector.onJavaScriptException(jsFuture::complete);

            driver.get("https://www.selenium.dev/selenium/web/bidi/logEntryAdded.html");
            driver.findElement(By.id("LogWithStacktrace")).click();

            JavascriptLogEntry jsLogEntry = jsFuture.get(10, TimeUnit.SECONDS);

            StackTrace stackTrace = jsLogEntry.getStackTrace();
            List<StackFrame> listOfStackFrames = stackTrace.getCallFrames();
            System.out.println(" JavaScript log: " + jsLogEntry.getText() + "\n");
            for (int a = 0; a < listOfStackFrames.size(); a++) {
                StackFrame stackFrame = listOfStackFrames.get(a);
                System.out.println(" Stack trace name " + (a + 1) + " of the login: " + stackFrame.getFunctionName());
            }

            Assertions.assertNotNull(stackTrace);
            Assertions.assertEquals(3, stackTrace.getCallFrames().size());


        }
    }

    @After
    public void closeBrowser() {
        driver.quit();
    }

}
