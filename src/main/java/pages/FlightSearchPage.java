package pages;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.ConfigReader;
import utils.ScreenshotUtil;

import java.time.Duration;
import java.util.List;

import static base.BaseTest.test;

public class FlightSearchPage {
    WebDriver driver;
    WebDriverWait wait;
    By closePopUp = By.xpath("//div[@class='pb-1 px-1 flex flex-middle nmx-1']//*[name()='svg']");
    By flightBookingLink = By.xpath("//h3[contains(text(), 'Flights')]");
    By tripDropDown = By.xpath("//span[contains(text(), 'One way')]");
    By roundTrip = By.xpath("//p[normalize-space()='Round trip']");
    By departDate = By.xpath("//div[contains(@class,'sc-aXZVg dSvAMK mr-2 mt-1')]//*[name()='svg']");
    By options = By.xpath("//ul[contains(@class, 'airportList')]//li");
    By monthText = By.xpath("//div[@class='DayPicker-Caption']/div");
    By nextMonthArrow = By.xpath("//*[name()='path' and contains(@d,'M10.869 2l')]");
    By priceList = By.xpath("//div[contains(@class, 'Datepicker--price')]");
    By dateList = By.xpath("./ancestor::div[contains(@class, 'DayPicker-Day')]");
    By searchFlight = By.xpath("//h4[text()='Search flights']");

    public FlightSearchPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void selectRoundTrip() {
        driver.findElement(closePopUp).click();
        WebElement flightLink = wait.until(ExpectedConditions.elementToBeClickable(flightBookingLink));
        try {
            flightLink.click();
        } catch (ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", flightLink);
        }
        driver.findElement(tripDropDown).click();
        driver.findElement(roundTrip).click();
        takeScreenshotIfEnabled("SelectRoundTrip", test.get());
    }

    public void selectDeparture() {
        driver.findElement(departDate).click();
    }

    public void searchFlight() {
        WebElement searchFlightButton = driver.findElement(searchFlight);
        JavascriptExecutor js= (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", searchFlightButton);
        js.executeScript("arguments[0].click();", searchFlightButton);
        takeScreenshotIfEnabled("ClickSearchFlight", test.get());
    }

    public void selectCityFromDropdown(String cityInput, String fieldPlaceholder) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement cityField = driver.findElement(By.cssSelector("input[placeholder='" + fieldPlaceholder + "']"));
        int maxRetries = 3;
        int attempt = 0;
        boolean citySelected = false;
        while (attempt < maxRetries && !citySelected) {
            try {
                cityField.click();
                cityField.sendKeys(Keys.CONTROL + "a");
                cityField.sendKeys(Keys.DELETE);
                Thread.sleep(1000);
                if(!cityField.getDomAttribute("value").isEmpty()){
                    for(int i=0;i<10;i++){
                        cityField.sendKeys(Keys.BACK_SPACE);
                    }
                    Thread.sleep(500);
                }
                cityField.sendKeys(cityInput);
                Thread.sleep(1000);
                // Wait for dropdown options
                List<WebElement> cityOptions = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(options));
                for (WebElement option : cityOptions) {
                    if (option.getText().contains(cityInput)) {
                        option.click();
                        citySelected = true;
                        System.out.println("Successfully selected city: " + cityInput);
                        return;
                    }
                }
                System.out.println("Attempt " + (attempt + 1) + ": No matching suggestion found for " + cityInput);
            } catch (TimeoutException e) {
                System.out.println("Attempt " + (attempt + 1) + ": Dropdown not found! Retrying...");
            } catch (StaleElementReferenceException e) {
                System.out.println("Stale Element Exception! Retrying input...");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            attempt++;
        }
        if (!citySelected) {
            throw new RuntimeException("City selection failed!" );
        }
    }

    public void selectLowestFareDate(String targetMonth) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        try {
            while (true) {
                WebElement monthElement = driver.findElement(monthText);
                String currentMonth = monthElement.getText();
                if (currentMonth.contains(targetMonth)) {
                    break;
                }
                WebElement nextMonthClick = wait.until(ExpectedConditions.elementToBeClickable(nextMonthArrow));
                nextMonthClick.click();
            }
            wait.until(ExpectedConditions.visibilityOfElementLocated(priceList));
            List<WebElement> prices = driver.findElements(priceList);
            if (prices.isEmpty()) {
                System.out.println("No prices displayed!");
                return;
            }
            int minIndex = -1;
            int minFare = Integer.MAX_VALUE;
            for (int i = 0; i < prices.size(); i++) {
                String priceText = prices.get(i).getText().replaceAll("[^0-9]", "");
                if (priceText.isEmpty()) {
                    System.out.println("Skipping non-numeric price value");
                    continue;
                }
                int price = Integer.parseInt(priceText);
                if (price < minFare) {
                    minFare = price;
                    minIndex = i;
                }
            }
            if (minIndex == -1) {
                System.out.println("Could not locate price");
                return;
            }
            System.out.println("Min Fare: " + minFare + " on index: " + minIndex);
            WebElement dateElement = prices.get(minIndex).findElement(dateList);
            JavascriptExecutor js = (JavascriptExecutor) driver;

            for (int i = 0; i < 3; i++) {
                try {
                    wait.until(ExpectedConditions.elementToBeClickable(dateElement)).click();
                    System.out.println("Successfully clicked lowest fare date");
                    break;
                } catch (StaleElementReferenceException e) {
                    System.out.println("Element became stale. Retrying...");
                } catch (ElementClickInterceptedException e) {
                    System.out.println("Normal click failed! Trying javascript click");
                    js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", dateElement);
                    js.executeScript("arguments[0].click();", dateElement);
                    break;
                }
            }
        } catch (StaleElementReferenceException e) {
            System.out.println("Element became stale for next month click. Retrying...");
        }
    }

    public void takeScreenshotIfEnabled(String action, ExtentTest test) {
        boolean screenshotEnabled = Boolean.parseBoolean(ConfigReader.getProperty("screenshotOnDemand"));
        if (screenshotEnabled) {
            String screenshotPath = ScreenshotUtil.takeScreenshot(driver, action);
            try {
                //Embeds the screenshot from the saved file path in the report
                test.info("Screenshot for : " + action,
                        MediaEntityBuilder.createScreenCaptureFromPath(screenshotPath).build());
            } catch (Exception e) {
                throw new RuntimeException("Failed to attach screenshot" + e.getMessage());
            }

        }
    }

}
