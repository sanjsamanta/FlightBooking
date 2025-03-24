package base;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.annotations.*;
import utils.ConfigReader;
import utils.ScreenshotUtil;

import java.time.Duration;

//ITestListener - Allows TestNG to listen for test execution events
public class BaseTest implements ITestListener {
    public static WebDriver driver;
    public static ExtentReports extent;
    public static ThreadLocal<ExtentTest> test = new ThreadLocal<>();

    @BeforeSuite
    public void setup(){
        ExtentSparkReporter spark = new ExtentSparkReporter("test-output/ExtentReport.html");
        extent = new ExtentReports();
        extent.attachReporter(spark);

        String url = ConfigReader.getProperty("URL");
        String browser = ConfigReader.getProperty("browser");
        switch (browser) {
            case "chrome":
                WebDriverManager.chromedriver().setup();
                driver = new ChromeDriver();
                break;

            case "edge":
                WebDriverManager.edgedriver().setup();
                driver = new EdgeDriver();
                break;

            case "safari":
                WebDriverManager.safaridriver().setup();
                driver = new SafariDriver();
                break;

            default:
                throw new IllegalArgumentException("Unsupported browser: "+browser);
        }
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().window().maximize();
        driver.get(url);
        //Creates a test log in Extent Report using the test class name
        test.set(extent.createTest(getClass().getSimpleName()));
    }

    @AfterMethod
    public void captureTestResult(ITestResult result) {
        String screenshotPath = ScreenshotUtil.takeScreenshot(driver, result.getName());
        //ITestResult - TestNG interface that provides runtime information about test method's execution
        if (result.getStatus() == ITestResult.FAILURE) {
            test.get().fail("Test failed - " +result.getMethod().getMethodName())
                    .fail("Exception: "+result.getThrowable().getMessage())
                    .addScreenCaptureFromPath(screenshotPath);
        }
        else if (result.getStatus() == ITestResult.SUCCESS) {
            test.get().pass("Test passed - " +result.getMethod().getMethodName())
                    .addScreenCaptureFromPath(screenshotPath);
        }
    }

    @AfterSuite
    public void tearDown() throws InterruptedException {
        Thread.sleep(20000);
        driver.quit();
        extent.flush();
    }
}
