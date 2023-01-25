package Entities;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.github.javafaker.Faker;

public class BookingDates {

    private static int MAX_FUTURE_BOOKING_DAYS = 30;
    private static int MAX_BOOKING_INTERVAL_IN_DAYS = 180;

    private String checkin;
    private String checkout;

    public BookingDates() {
        Faker faker = new Faker();
        Date startDate =
                faker.date().future(MAX_FUTURE_BOOKING_DAYS, 1, TimeUnit.DAYS);
        Date endDate = faker.date().future(MAX_BOOKING_INTERVAL_IN_DAYS,
                TimeUnit.DAYS, startDate);

        this.checkin = startDate.toString();
        this.checkout = endDate.toString();
    }

    public String getCheckin() {
        return checkin;
    }

    public void setCheckin(String checkin) {
        this.checkin = checkin;
    }

    public String getCheckout() {
        return checkout;
    }

    public void setCheckout(String checkout) {
        this.checkout = checkout;
    }
}
