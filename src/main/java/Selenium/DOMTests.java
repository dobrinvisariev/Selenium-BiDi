package Selenium;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.bidi.browsingcontext.BrowsingContext;
import org.openqa.selenium.bidi.browsingcontext.BrowsingContextInfo;
import org.openqa.selenium.bidi.browsingcontext.ReadinessState;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.List;

public class DOMTests {

    private WebDriver driver;
    BrowsingContext parentWindow;
    private List<BrowsingContextInfo> windowContextInfo;

    @Before
    public void browserDriverSetUp() {
        ChromeOptions options = new ChromeOptions();
        // Enable the Communication between the WebDriver with the browser
        options.setCapability("webSocketUrl", true);
        driver = new ChromeDriver(options);
        // Get the current window identifier
        String windowIdentifier = driver.getWindowHandle();
        System.out.println("windowIdentifier = " + windowIdentifier);
        // Initialize browsing context using the driver and the window identifier
        parentWindow = new BrowsingContext(driver, windowIdentifier);
        System.out.println("parentWindow = " + parentWindow);
    }

    @Test
    public void getDOMTree() {

        parentWindow.navigate("https://www.selenium.dev/selenium/web/iframes.html", ReadinessState.COMPLETE);
        windowContextInfo = parentWindow.getTree();

        System.out.println("Window Context Info: " + windowContextInfo.size());


    }
    
    @After
    public void closeBrowser() throws InterruptedException {
        Thread.sleep(500);
        driver.quit();
    }

}
