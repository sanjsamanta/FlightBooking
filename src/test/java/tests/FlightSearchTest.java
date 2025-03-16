package tests;

import base.BaseTest;
import org.testng.annotations.Test;
import pages.FlightSearchPage;
import utils.ExcelUtils;

public class FlightSearchTest extends BaseTest {

    @Test
    public void testFlightBooking() {

        FlightSearchPage flightSearchPage = new FlightSearchPage(driver);
        flightSearchPage.selectRoundTrip();
        flightSearchPage.selectCityFromDropdown(ExcelUtils.readData(1,0), "Where from?");
        flightSearchPage.selectCityFromDropdown(ExcelUtils.readData(1,1), "Where to?");
        flightSearchPage.selectDeparture();
        flightSearchPage.selectLowestFareDate(ExcelUtils.readData(1,2));
        flightSearchPage.selectLowestFareDate(ExcelUtils.readData(1,3));
        flightSearchPage.searchFlight();

    }

}
