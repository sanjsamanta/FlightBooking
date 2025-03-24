package tests;

import base.BaseTest;
import com.aventstack.extentreports.ExtentTest;
import org.testng.annotations.Test;
import pages.FlightSearchPage;
import utils.ExcelUtils;

import java.io.IOException;
import java.util.Map;

public class FlightSearchTest extends BaseTest {

    @Test(priority = 1)
    public void testFlightSearch() throws IOException {

        FlightSearchPage flightSearchPage = new FlightSearchPage(getDriver());

        flightSearchPage.selectRoundTrip();
        ExcelUtils excelUtils = new ExcelUtils("Sheet1");
        int rowCount = excelUtils.getRowCount();
        for (int i = 1; i <= rowCount; i++) {
            Map<String, String> rowData = excelUtils.getRowData(i);
            String source = rowData.get("Source");
            String destination = rowData.get("Destination");
            String deptMonth = rowData.get("DeptMonth");
            String returnMonth = rowData.get("ReturnMonth");

            flightSearchPage.selectCityFromDropdown(source, "Where from?");
            flightSearchPage.selectCityFromDropdown(destination, "Where to?");
            flightSearchPage.selectDeparture();
            flightSearchPage.selectLowestFareDate(deptMonth);
            flightSearchPage.selectLowestFareDate(returnMonth);
            flightSearchPage.searchFlight();
        }
    }
}
