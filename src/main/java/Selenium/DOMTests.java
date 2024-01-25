package Selenium;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchShadowRootException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.bidi.browsingcontext.BrowsingContext;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.HasLogEvents;

import java.time.Duration;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.openqa.selenium.devtools.events.CdpEventTypes.domMutation;

public class DOMTests {

    private WebDriver driver;
    BrowsingContext browserWindow;
    String windowIdentifier;
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

    public WebElement findElementInShadowDOM(By selector) {
        WebElement shadowRoot = driver.findElement(By.id("usercentrics-root"));
        SearchContext shadowDom = null;
        try {
            shadowDom = shadowRoot.getShadowRoot();
        } catch (NoSuchShadowRootException e) {
            System.out.println("No shadow root found");
        }
        assert shadowDom != null;
        return shadowDom.findElement(selector);
    }

    @Test
    public void mutationDOMElements() throws InterruptedException {

        driver.navigate().to("https://www.trivago.co.uk/en-GB/lm/hotels-sofia-bulgaria?search=200-15136;dr-20240815-20240816");
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        domMutations = new CopyOnWriteArrayList<>();

        ((HasLogEvents) driver).onLogEvent(domMutation(e -> domMutations.add(e.getElement())));

        findElementInShadowDOM(By.cssSelector("[data-testid='uc-accept-all-button']")).click();

        System.out.println("OLD DOM MUTATIONS: " + domMutations.size() + "\n");

        int intervalOfSecondsBetweenTests = 1, maxConsecutiveChecks = 10, minChecks = 3, numberOfConsecutiveNoMutationsIntervalsDetected = 0, checks = 0;
        int oldDomMutationsSize = domMutations.size(), newDomMutationsSize;
        int i = 1;

        // Wait for DOM to be stable for trivago webpage
        while (checks <= maxConsecutiveChecks) {
            Thread.sleep(intervalOfSecondsBetweenTests * 1000);
            checks++;
            newDomMutationsSize = domMutations.size();

            System.out.println(" NEW DOM MUTATIONS: " + newDomMutationsSize + "\n");

            if (oldDomMutationsSize == newDomMutationsSize) {

                System.out.println("Check " + i + ": DOM might be stable, rechecking again... " + "\n");
                numberOfConsecutiveNoMutationsIntervalsDetected++;
                i++;
            } else {
                oldDomMutationsSize = newDomMutationsSize;
                System.out.println("There is a change in the DOM! ");
                System.out.println("Reasigning old DOM mutations size with the new mutation size: " + oldDomMutationsSize + "\n");
                numberOfConsecutiveNoMutationsIntervalsDetected = 0;
            }
            if (numberOfConsecutiveNoMutationsIntervalsDetected == minChecks) {
                System.out.println("DOM is stable! " + "\n");
                break;
            }
        }
    }

    @After
    public void closeBrowser() throws InterruptedException {
        Thread.sleep(100);
        driver.quit();
    }

}
