package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.FlightSelectPage;
import pages.ItineraryPage;

public class FlightSelectTest extends BaseTest {

    @Test(priority = 2)
    public void validateFlightSelectionAndPrice() throws InterruptedException {

        FlightSelectPage flightSelectPage = new FlightSelectPage(getDriver());
        ItineraryPage itineraryPage = new ItineraryPage(getDriver());
        flightSelectPage.selectNonStop();
        flightSelectPage.selectEarlyMorningOnward();
        flightSelectPage.selectEarlyMorningReturn();
        flightSelectPage.selectEarlyMorningOnwardFlight();
        flightSelectPage.selectEarlyMorningReturnFlight();
        String flightPrice = flightSelectPage.getFlightPrice();
        flightSelectPage.clickBookNow();
        String itineraryPrice = itineraryPage.getItineraryPrice();
        Assert.assertEquals(flightPrice, itineraryPrice);
    }
}
