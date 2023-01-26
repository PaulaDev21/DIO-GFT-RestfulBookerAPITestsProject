package Entities;

public class BookingDates {

    private String checkin;
    private String checkout;

    public BookingDates(String startDate, String endDate) {
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
