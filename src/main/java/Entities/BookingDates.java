package Entities;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.github.javafaker.Faker;

public class BookingDates {

    private String checkin;
    private String checkout;

    public BookingDates(String startDate, String endDate) {
        Faker faker = new Faker();


        this.checkin = startDate;
        this.checkout = endDate;
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
