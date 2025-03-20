package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class ItineraryPage {
    WebDriver driver;
    WebDriverWait wait;

    public ItineraryPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    By itineraryPrice = By.xpath("//p[@class='fs-6 fw-700']");

    public String getItineraryPrice() {
        WebElement itineraryPriceElement = wait.until(ExpectedConditions.visibilityOfElementLocated(itineraryPrice));
        return itineraryPriceElement.getText().replaceAll("[^0-9]", "");
    }
}
