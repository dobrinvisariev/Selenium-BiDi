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
import org.openqa.selenium.bidi.log.StackFrame;
import org.openqa.selenium.bidi.log.StackTrace;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LogTests {

    private WebDriver driver;

    @Before
    public void browserDriverSetUp() {
        ChromeOptions options = new ChromeOptions();
        // Enable the Communication between the WebDriver with the browser
        options.setCapability("webSocketUrl", true);
        driver = new ChromeDriver(options);
    }

    @After
    public void closeBrowser() {
        driver.quit();
    }

    @Test
    public void testConsoleLogListening() {

        CopyOnWriteArrayList<ConsoleLogEntry> logsList = new CopyOnWriteArrayList<>();

        try (LogInspector logInspector = new LogInspector(driver)) {
            logInspector.onConsoleEntry(logsList::add);
        }
        driver.get("https://www.selenium.dev/selenium/web/bidi/logEntryAdded.html");
        driver.findElement(By.id("consoleLog")).click();
        // wait until the logs list has some entry
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(_d -> !logsList.isEmpty());
        Assertions.assertEquals("Hello, world!", logsList.get(0).getText());


        for (ConsoleLogEntry log : logsList) {
            System.out.println(" Log list: " + log.getText() + "\n");
        }
    }

    @Test
    public void testToListenToJSLogAndConsoleLog() throws InterruptedException, ExecutionException, TimeoutException {

        CompletableFuture<ConsoleLogEntry> consoleFutureResult = new CompletableFuture<>();
        CompletableFuture<JavascriptLogEntry> jsFutureResult = new CompletableFuture<>();

        try (LogInspector logInspector = new LogInspector(driver)) {
            logInspector.onConsoleEntry(consoleFutureResult::complete);
            logInspector.onJavaScriptLog(jsFutureResult::complete);

            driver.get("https://www.selenium.dev/selenium/web/bidi/logEntryAdded.html");
            driver.findElement(By.id("consoleLog")).click();
            driver.findElement(By.id("jsException")).click();

            ConsoleLogEntry logs = consoleFutureResult.get(10, TimeUnit.SECONDS);
            JavascriptLogEntry jsLogs = jsFutureResult.get(10, TimeUnit.SECONDS);

            System.out.println(" Console log entry: " + logs.getText() + "\n");
            System.out.println(" JavaScript log entry: " + jsLogs.getText() + "\n");

            //console log entry assertions
            Assertions.assertEquals("Hello, world!", logs.getText());
            Assertions.assertNull(logs.getRealm());
            Assertions.assertEquals(1, logs.getArgs().size());
            Assertions.assertEquals("console", logs.getType());
            Assertions.assertEquals("log", logs.getMethod());

            //JavaScript log entry assertions
            Assertions.assertEquals("Error: Not working", jsLogs.getText());
            Assertions.assertEquals("javascript", jsLogs.getType());
            Assertions.assertEquals(LogLevel.ERROR, jsLogs.getLevel());
        }
    }

    @Test
    public void stackTraceForALog() throws TimeoutException, ExecutionException, InterruptedException {

        CompletableFuture<JavascriptLogEntry> jsFutureEntry = new CompletableFuture<>();

        try (LogInspector logInspector = new LogInspector(driver)) {
            logInspector.onJavaScriptException(jsFutureEntry::complete);

            driver.get("https://www.selenium.dev/selenium/web/bidi/logEntryAdded.html");
            driver.findElement(By.id("LogWithStacktrace")).click();

            JavascriptLogEntry jsLogEntry = jsFutureEntry.get(10, TimeUnit.SECONDS);

            StackTrace stackTrace = jsLogEntry.getStackTrace();
            List<StackFrame> listOfStackFrames = stackTrace.getCallFrames();
            System.out.println(" JavaScript log: " + jsLogEntry.getText() + "\n");
            for (int a = 0; a < listOfStackFrames.size(); a++) {
                StackFrame stackFrame = listOfStackFrames.get(a);
                System.out.println(" Stack trace " + (a + 1) + " " + stackFrame.getFunctionName() + "\n");
            }
            Assertions.assertNotNull(stackTrace);
            Assertions.assertEquals(3, stackTrace.getCallFrames().size());
        }
    }
}
