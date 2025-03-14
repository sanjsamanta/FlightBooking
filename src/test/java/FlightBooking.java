package src.test.java;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileInputStream;
import java.time.Duration;
import java.util.List;

public class FlightBooking {
    public static WebDriver driver;
   public static void main(String[] args){
        try {
            String excelPath = "/Users/sanjsamanta/Downloads/FlightData.xlsx";
            String[] fileData = readData(excelPath);
            String source = fileData[0];
            String destination = fileData[1];
            String fromMonth = fileData[2];
            String toMonth = fileData[3];

            WebDriverManager.chromedriver().setup();
            driver = new ChromeDriver();
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
            driver.get("https://www.cleartrip.com/");
            driver.manage().window().maximize();
            WebDriverWait wait=new WebDriverWait(driver, Duration.ofSeconds(30));

            WebElement closePopUp=driver.findElement(By.xpath("//div[@class='pb-1 px-1 flex flex-middle nmx-1']//*[name()='svg']"));
            closePopUp.click();
            WebElement tripDropdown = driver.findElement(By.xpath("//span[contains(text(), 'One way')]"));
            tripDropdown.click();
            WebElement roundTrip = driver.findElement(By.xpath("//p[normalize-space()='Round trip']"));
            roundTrip.click();

            selectCityFromDropdown(driver, source, "Where from?");
            selectCityFromDropdown(driver, destination, "Where to?");

            //Click on Departure date field
            driver.findElement(By.xpath("//div[contains(@class,'sc-aXZVg dSvAMK mr-2 mt-1')]//*[name()='svg']")).click();
            selectLowestFareDate(driver,fromMonth);

            //Click on Return date field
            selectLowestFareDate(driver, toMonth);

            WebElement searchFlightButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//h4[text()='Search flights']")));
            JavascriptExecutor js= (JavascriptExecutor) driver;
            js.executeScript("arguments[0].scrollIntoView({block: 'center'});", searchFlightButton);
            js.executeScript("arguments[0].click();", searchFlightButton);
            Thread.sleep(2000);
            driver.quit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void selectCityFromDropdown(WebDriver driver, String cityInput, String fieldPlaceholder) {
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
                List<WebElement> options = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//ul[contains(@class, 'airportList')]//li")));
                for (WebElement option : options) {
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
            System.out.println("Using fallback selection for: " + cityInput);
            cityField.sendKeys(Keys.ARROW_DOWN);
            cityField.sendKeys(Keys.ENTER);
        }
    }

    public static String[] readData(String filePath){
        String[] data =new String[5];
        try{
            FileInputStream file=new FileInputStream(filePath);
            Workbook workbook=new XSSFWorkbook(file);
            Sheet sheet=workbook.getSheetAt(0);
            Row row=sheet.getRow(1);
            data[0]=row.getCell(0).getStringCellValue();
            data[1]=row.getCell(1).getStringCellValue();
            data[2]=row.getCell(2).getStringCellValue();
            data[3]=row.getCell(3).getStringCellValue();
            workbook.close();
            file.close();
        } catch(Exception e){
            e.printStackTrace();
        }
        return data;
    }
    public static void selectLowestFareDate(WebDriver driver, String targetMonth) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            while (true) {
                WebElement monthElement = driver.findElement(By.xpath("//div[@class='DayPicker-Caption']/div"));
                String currentMonth = monthElement.getText();
                if (currentMonth.contains(targetMonth)) {
                    break;
                }
                WebElement nextMonthArrow = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[name()='path' and contains(@d,'M10.869 2l')]")));
                nextMonthArrow.click();
            }
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class, 'Datepicker--price')]")));
            List<WebElement> prices = driver.findElements(By.xpath("//div[contains(@class, 'Datepicker--price')]"));
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
            if(minIndex==-1){
                System.out.println("Could not locate price");
                return;
            }
            System.out.println("Min Fare: " + minFare + " on index: " + minIndex);

            WebElement dateElement = prices.get(minIndex).findElement(By.xpath("./ancestor::div[contains(@class, 'DayPicker-Day')]"));
            JavascriptExecutor js = (JavascriptExecutor) driver;
            try {
                wait.until(ExpectedConditions.elementToBeClickable(dateElement));
                dateElement.click();
                System.out.println("Successfully clicked lowest fare date");
            } catch (Exception e) {
                System.out.println("Normal click failed! Trying javascript click");
                js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", dateElement);
                js.executeScript("arguments[0].click();", dateElement);
            }
        }catch (Exception e){
            System.out.println("Couldn't select the date with lowest fare");
        }

    }

}