import Entities.Booking;
import Entities.BookingDates;
import Entities.User;
import com.github.javafaker.Faker;

import io.restassured.RestAssured;
import io.restassured.filter.log.ErrorLoggingFilter;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

import org.apache.commons.lang3.ObjectUtils.Null;
import org.junit.jupiter.api.*;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static io.restassured.config.LogConfig.logConfig;
import static io.restassured.module.jsv.JsonSchemaValidator.*;
import static org.hamcrest.Matchers.*;

public class BookingTests {
    private static int MAX_FUTURE_BOOKING_DAYS = 30;
    private static int MAX_BOOKING_INTERVAL_IN_DAYS = 180;
    public static Faker faker;
    private static RequestSpecification request;
    private static Booking booking;
    private static BookingDates bookingDates;
    private static User user;
    public static String bookingRequestSchema = "CreateBookingRequestSchema.json";
    public static String token = "";

    @BeforeAll
    public static void Setup() {
        RestAssured.baseURI = "https://restful-booker.herokuapp.com";
        faker = new Faker();
        user = new User(faker.name().username(), faker.name().firstName(), faker.name().lastName(),
                faker.internet().safeEmailAddress(), faker.internet().password(8, 10),
                faker.phoneNumber().toString());

        Date startDate = faker.date().future(MAX_FUTURE_BOOKING_DAYS, 1, TimeUnit.DAYS);
        Date endDate = faker.date().future(MAX_BOOKING_INTERVAL_IN_DAYS, TimeUnit.DAYS, startDate);
        bookingDates = new BookingDates(startDate.toString(), endDate.toString());
        booking = new Booking(user.getFirstName(), user.getLastName(),
                (float) faker.number().randomDouble(2, 50, 100000), true, bookingDates,
                "non smoking");
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter(),
                new ErrorLoggingFilter());
    }

    @BeforeEach
    void setRequest() {
        RestAssured.defaultParser = Parser.JSON;

        /*
         * request = given() .config(RestAssured.config().logConfig(
         * logConfig().enableLoggingOfRequestAndResponseIfValidationFails()))
         * .contentType(ContentType.JSON) .auth() .basic("admin", "password123");
         */
        request = given().config(RestAssured.config()).contentType(ContentType.JSON);
        // .auth().basic(user.getUsername(), user.getPassword());
    }

    @Test
    public void authentication_WithInvalidUserName() {
        Response response = getToken("invader", "password123");

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals("Bad credentials", token);
    }

    @Test
    public void authentication_WithInvalidPassword() {
        Response response = getToken("admin", "wrongpass");

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals("Bad credentials", token);
    }

    @Test
    public void authentication_WithInvalidNameAndPassword() {
        Response response = getToken("invader", "wrongpass");

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals("Bad credentials", token);
    }

    @Test
    public void authentication_WithValidUserData() {
        Response response = getToken("admin", "password123");

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertNotEquals("Bad credentials", token);
    }


    @Test
    public void getBookingById_WithInvalidId_returnNotFound() {
        request.when().get("/booking/9999999").then().assertThat().statusCode(404);
    }

    @Test
    public void CreateBooking_WithValidData_returnOk() {
        getToken("admin", "password123");
        given().header("Authorization", "Bearer " + token).contentType("application/json")
                .body(booking).when().post("/booking").then().assertThat().statusCode(200)
                .contentType(ContentType.JSON).and().time(lessThan(10000L));
    }

    @Test
    public void updateBooking_WithInvalidId_returnNotAllowed() {
        given().header("Authorization", "Basic " + "YWRtaW46cGFzc3dvcmQxMjM=")
                .contentType("application/json").body(booking).when().put("/booking/9999999").then()
                .assertThat().statusCode(405);// not allowed
    }

    @Test
    public void updateBooking_WithValidData_returnOk() {
        // create book
        int bookingId = given().contentType("application/json").body(booking).when()
                .post("/booking").then().extract().jsonPath().getInt("bookingid");

        // Change booking data
        booking.setDepositpaid(false);

        // update booking in the server
        given().header("Authorization", "Basic " + "YWRtaW46cGFzc3dvcmQxMjM=")
                .contentType("application/json").body(booking).when().put("/booking/" + bookingId)
                .then().assertThat().statusCode(200);

        // Verify the booking was updated
        request.when().get("/booking/" + bookingId).then().assertThat().statusCode(200)
                .contentType(ContentType.JSON).and().body("depositpaid", equalTo(false));
    }


    @Test
    public void deleteBooking_WithInvalidId_returnNotAllowed() {
        given().header("Authorization", "Basic " + "YWRtaW46cGFzc3dvcmQxMjM=")
                .contentType("application/json").body(booking).when().delete("/booking/999999")
                .then().assertThat().statusCode(405);
    }

    @Test
    public void deleteBooking_WithValidId_returnCreated() {
        // create book
        int bookingId = given().contentType("application/json").body(booking).when()
                .post("/booking").then().extract().jsonPath().getInt("bookingid");

        // delete book just created
        given().header("Authorization", "Basic " + "YWRtaW46cGFzc3dvcmQxMjM=")
                .contentType("application/json").body(booking).when()
                .delete("/booking/" + bookingId).then().assertThat().statusCode(201);
    }


    @Test
    public void getAllBookingsById_returnOk() {
        Response response = request.when().get("/booking").then().extract().response();

        Assertions.assertNotNull(response);
        Assertions.assertEquals(200, response.statusCode());
    }

    @Test
    public void getAllBookingsByUserFirstName_BookingExists_returnOk() {
        request.when().queryParam("firstName", user.getFirstName()).get("/booking").then()
                .assertThat().statusCode(200).contentType(ContentType.JSON).and()
                .body("results", hasSize(greaterThan(0)));
    }

    private Response getToken(String username, String password) {
        String jsonLoginStructure =
                "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
        Response response = given().contentType("application/json").body(jsonLoginStructure).when()
                .post("/auth").then().extract().response();
        token = response.body().path("token");
        if (Objects.isNull(token)) {
            token = response.body().path("reason");
        }
        return response;
    }



}
