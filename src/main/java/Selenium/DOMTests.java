package Selenium;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.bidi.browsingcontext.BrowsingContext;
import org.openqa.selenium.bidi.browsingcontext.BrowsingContextInfo;
import org.openqa.selenium.bidi.browsingcontext.ReadinessState;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.HasLogEvents;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static org.openqa.selenium.devtools.events.CdpEventTypes.domMutation;

public class DOMTests {

    private WebDriver driver;
    BrowsingContext browserWindow;
    String windowIdentifier;
    private List<BrowsingContextInfo> windowContextInfoList;
    private CopyOnWriteArrayList<WebElement> domMutations;

    @Before
    public void browserDriverSetUp() {
        ChromeOptions options = new ChromeOptions();
        // Enable the Communication between the WebDriver with the browser
        options.setCapability("webSocketUrl", true);
        driver = new ChromeDriver(options);

        // Get the current window identifier
        windowIdentifier = driver.getWindowHandle();

        // Initialize browsing context using the driver and the window identifier
        browserWindow = new BrowsingContext(driver, windowIdentifier);
    }


    public void browsingContextTest() {

        browserWindow.navigate("https://www.trivago.co.uk", ReadinessState.COMPLETE);
        //Stores the number of open windows and/or tabs

        windowContextInfoList = browserWindow.getTree();

        System.out.println("Window Context Info List: " + windowContextInfoList.size() + "\n");
        
        BrowsingContextInfo windowInfo = windowContextInfoList.get(0);
        Assertions.assertEquals(0, windowInfo.getChildren().size());
        Assertions.assertEquals(1, windowContextInfoList.size());
        Assertions.assertEquals(windowIdentifier, windowInfo.getId());
        Assertions.assertTrue(windowInfo.getUrl().contains("trivago"));



    }

    @Test
    public void mutationDOMElements() {

        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        driver.get("https://www.trivago.co.uk");
        //driver.navigate().to("https://www.trivago.co.uk");

        domMutations = new CopyOnWriteArrayList<>();

        ((HasLogEvents) driver).onLogEvent(domMutation(e -> domMutations.add(e.getElement())));

        driver.findElement(By.cssSelector("[data-test-id=uc-accept-all-button]")).click();
    }

   @After
    public void closeBrowser() throws InterruptedException {
        Thread.sleep(500);
        driver.quit();
    }

}
