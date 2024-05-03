package Selenium;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v85.performance.Performance;
import org.openqa.selenium.devtools.v85.performance.model.Metric;
import org.openqa.selenium.edge.EdgeDriver;

import java.util.List;
import java.util.Optional;

public class PerformanceMetricsTest {

    EdgeDriver driver;
    private List<Metric> performanceMetricsList;

    @Before
    public void browserDriverSetup() {
        driver = new EdgeDriver();
        DevTools devTools = driver.getDevTools();
        devTools.createSession();
        devTools.send(Performance.enable(Optional.empty()));
        performanceMetricsList = devTools.send(Performance.getMetrics());
        //BiDi bidiTools = driver.getBiDi();
        //bidiTools.send(Performance.enable(Optional.empty()));
    }

    @After
    public void closeBrowser() {
        driver.quit();
    }

    @Test
    public void performanceTest() throws InterruptedException {
        driver.get("https://www.trivago.com/en-US/lm/hotels-varna-bulgaria?search=200-15139");
        Thread.sleep(7000);
        for (Metric metric : performanceMetricsList) {
            System.out.println("Performance Metric: " + metric.getName() + " - " + metric.getValue() + "\n");
        }
    }
}
