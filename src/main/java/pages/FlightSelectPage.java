package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

public class FlightSelectPage {

    WebDriver driver;
    WebDriverWait wait;

    By nonStopFilter = By.xpath("(//span[@class='checkbox__mark bs-border bc-neutral-500 bw-1 ba'])[1]");
    By earlyMorningOnwardFilter = By.xpath("(//span[@class='checkbox__mark bs-border bc-neutral-500 bw-1 ba'])[4]");
    By earlyMorningReturnFilter = By.xpath("(//span[@class='checkbox__mark bs-border bc-neutral-500 bw-1 ba'])[9]");
    By onwardDepartureTimes = By.xpath("//div[@data-test-attrib='onward-view']//div[contains(@class, 'flex-bottom') and contains(@class, 'ms-grid-column-1')][1]");
    By returnDepartureTimes = By.xpath("//div[@data-test-attrib='return-view']//div[contains(@class, 'flex-bottom') and contains(@class, 'ms-grid-column-1')][1]");
    By bookNow = By.xpath("//span[contains(text(), 'Book now')]");
    By flightPrice = By.xpath("//span[@class='c-neutral-900 fw-700 flex flex-right fs-6']");

    public FlightSelectPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    public void selectNonStop() {
        WebElement nonStop = wait.until(ExpectedConditions.elementToBeClickable(nonStopFilter));
        nonStop.click();
    }

    public void selectEarlyMorningOnward() {
        driver.findElement(earlyMorningOnwardFilter).click();
    }

    public void selectEarlyMorningReturn() {
        WebElement earlyMorFilter = wait.until(ExpectedConditions.elementToBeClickable(earlyMorningReturnFilter));
        try {
            earlyMorFilter.click();
        } catch (ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", earlyMorFilter);
        }
    }

    public void selectEarlyMorningOnwardFlight() {
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(onwardDepartureTimes));
        List<WebElement> onwardDeptTimes = driver.findElements(onwardDepartureTimes);
        String earliestTime = "23:59";
        WebElement earliestFlight = null;

        for(WebElement timeElement: onwardDeptTimes) {
            String currentTime = timeElement.getText().trim();
            if(currentTime.compareTo(earliestTime) < 0) {
                earliestTime = currentTime;
                earliestFlight = timeElement;
            }
        }
        if(earliestFlight != null) {
            WebElement earlyFlight = wait.until(ExpectedConditions.elementToBeClickable(earliestFlight));
            JavascriptExecutor js= (JavascriptExecutor) driver;
            js.executeScript("arguments[0].scrollIntoView({block: 'center'});", earlyFlight);
            js.executeScript("arguments[0].click();", earlyFlight);
            System.out.println("Clicked on the earliest onward flight departing at: " +earliestTime);
        } else {
            throw new RuntimeException("No onward flights found!");
        }
    }

    public void selectEarlyMorningReturnFlight() {
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(returnDepartureTimes));
        List<WebElement> returnDeptTimes = driver.findElements(returnDepartureTimes);
        String earliestTime = "23:59";
        WebElement earliestFlight = null;

        for (WebElement timeElement : returnDeptTimes) {
            String currentTime = timeElement.getText().trim();
            if (currentTime.compareTo(earliestTime) < 0) {
                earliestTime = currentTime;
                earliestFlight = timeElement;
            }
        }
        if (earliestFlight != null) {
            WebElement earlyFlight = wait.until(ExpectedConditions.elementToBeClickable(earliestFlight));
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].scrollIntoView({block: 'center'});", earlyFlight);
            js.executeScript("arguments[0].click();", earlyFlight);
            System.out.println("Clicked on the earliest return flight departing at: " + earliestTime);
        } else {
            throw new RuntimeException("No return flights found!");
        }
    }

    public void clickBookNow() throws InterruptedException {
        WebElement bookNowClick = wait.until(ExpectedConditions.elementToBeClickable(bookNow));
        int attempts = 0;
        boolean success = false;
        String mainWindow = driver.getWindowHandle();
        while (attempts < 3 && !success) {
            try {
                bookNowClick.click();
                System.out.println("Normal click worked!");
                success = true;
            } catch (ElementClickInterceptedException e) {
                System.out.println("Using Javascript click!!");
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript("arguments[0].scrollIntoView({block: 'center'});", bookNowClick);
                Thread.sleep(2000);
                js.executeScript("arguments[0].click();", bookNowClick);
                success = true;
            } catch (StaleElementReferenceException e) {
                System.out.println("Stale element exception! Re-locating element");
                bookNowClick = wait.until(ExpectedConditions.elementToBeClickable(bookNow));
            }
            Thread.sleep(3000);

            Set<String> allWindows = driver.getWindowHandles();
            if (allWindows.size() > 1) {
                for (String window : allWindows) {
                    if (!window.equals(mainWindow)) {
                        driver.switchTo().window(window);
                        System.out.println("Switched to new window: " + driver.getCurrentUrl());

                        if (driver.getCurrentUrl().contains("itinerary/failure")) {
                            System.out.println("Server error found! Closing error page and retrying...");
                            driver.close();
                            driver.switchTo().window(mainWindow);
                            driver.navigate().refresh();
                            Thread.sleep(5000);
                            attempts++;
                            success = false;
                            break;
                        } else {
                            System.out.println("Navigated to Itinerary page!");
                            return;
                        }
                    }
                }
            }
        }
        if(!success) {
            throw new RuntimeException("Book now click failed after maximum retries!");
        } else {
            System.out.println("Navigated to Itinerary page!!!");
        }
    }

    public String getFlightPrice() {
        WebElement flightPriceElement = wait.until(ExpectedConditions.visibilityOfElementLocated(flightPrice));
        return flightPriceElement.getText().replaceAll("[^0-9]", "");
    }
}
