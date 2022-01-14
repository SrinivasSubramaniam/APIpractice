package org.example.org.example;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import requestEntity.BookingDates;
import requestEntity.RequestEntityBuilder;
import static org.hamcrest.Matchers.*;
import static io.restassured.module.jsv.JsonSchemaValidator.*;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class RestFulBooker {

    public HashMap<String, String>headers=new HashMap<>();
    public static int bookingId;
    public static String authToken="";
    public RequestEntityBuilder requestEntityBuilder=new RequestEntityBuilder();
    RequestEntityBuilder update=new RequestEntityBuilder();
    public OauthPojo oauth;
    
    //BookingDates dates=new BookingDates();

    @BeforeSuite
    public void createToken() throws JsonParseException, JsonMappingException, IOException{
        RestAssured.baseURI="https://restful-booker.herokuapp.com";
        Response auth = RestAssured.given().log().all()
        		.contentType(ContentType.JSON)
                .body(authInfoPojo())
                .post("auth");
        JsonPath path = auth.jsonPath();
        authToken=path.get("token");
        auth.then().extract().response();
        
     /*  RestAssured
       .given()
       .multiPart("",new File(""),"application/json")
       .get()
       .getContentType();*/

    }


    @BeforeMethod
    public void setUp(){
        headers.put("Content-Type","application/json");
        headers.put("Accept", "application/json");
        headers.put("Cookie","token="+authToken);

    }

   @Test
    public void createBooking() throws ParseException {
        Response booking = RestAssured.given().log().all()
                .headers(headers)
                .when()
                .body(createFormBody())
                .post("/booking")
                .then()
                .extract().response();
        JsonPath jsonPath = booking.jsonPath();
        booking.prettyPrint();
        bookingId= jsonPath.get("bookingid");

    }
    @Test(dependsOnMethods = "createBooking")
    public void updateBooking() throws ParseException, JsonParseException, JsonMappingException, IOException{
        Response booking = RestAssured.given().log().all()
        		//.accept("application/json")
        		//.contentType(ContentType.JSON)
                .headers(headers)
                .when()
                .body(updaterequestSpec())
                .put("booking/"+bookingId)
                .then()
                .extract().response();
        //booking.then().assertThat().body("", containsString(""));
        int statusCode = booking.statusCode();
        System.out.println("status code is "+statusCode);
        JsonPath jsonPath = booking.jsonPath();
        booking.prettyPrint();
       /*booking.then().assertThat()
       .body(matchesJsonSchema("{}"));*/
       
       
        Assert.assertEquals(jsonPath.get("totalprice"),update.getTotalprice());
        Assert.assertEquals(jsonPath.get("bookingdates.checkin"),update.getBookingdates().getCheckin());
       if ( jsonPath.get("totalprice") instanceof Integer) System.out.println("Yes it is a date");
       else System.out.println("Not a date");
    }
    //@Test(dependsOnMethods = "createBooking")
    public void getBooking() throws ParseException {
    	File file=new File("./getFrom.json");
    	RestAssured.given().log().all()
    	.headers(headers)
    	.accept("application/json")
    	.when()
    	.get("booking/"+1)
    	.then()
    	.assertThat()
    	.body(matchesJsonSchema(file))
    	.extract()
    	.response()
    	;
    }

    public String createFormBody(){
        return "{\n" +
                "    \"firstname\" : \"Jim\",\n" +
                "    \"lastname\" : \"Brown\",\n" +
                "    \"totalprice\" : 111,\n" +
                "    \"depositpaid\" : true,\n" +
                "    \"bookingdates\" : {\n" +
                "        \"checkin\" : \"2018-01-01\",\n" +
                "        \"checkout\" : \"2019-01-01\"\n" +
                "    },\n" +
                "    \"additionalneeds\" : \"Dinner\"\n" +
                "}";
    }
    public String getFromBody(){
        return "{\n" + 
        		"    \"firstname\": \"Eric\",\n" + 
        		"    \"lastname\": \"Wilson\",\n" + 
        		"    \"totalprice\": 826,\n" + 
        		"    \"depositpaid\": false,\n" + 
        		"    \"bookingdates\": {\n" + 
        		"        \"checkin\": \"2019-05-14\",\n" + 
        		"        \"checkout\": \"2020-04-03\"\n" + 
        		"    }\n" + 
        		"}";
    }
    public String updateFormBody(){
        return "{\n" + 
        		/*"    \"firstname\": \"James\",\n" + 
        		"    \"lastname\": \"Brown\",\n" + 
        		"    \"totalprice\": 161,\n" + */
        		"    \"depositpaid\": true,\n" + 
        		"    \"bookingdates\": {\n" + 
        		"        \"checkin\": \"2018-01-01\",\n" + 
        		"        \"checkout\": \"2019-01-01\"\n" + 
        		"    },\n" + 
        		"    \"additionalneeds\": \"Dinner1030\"\n" + 
        		"}";
    }
    public RequestEntityBuilder requestSpec() throws ParseException {
    	 
       BookingDates dates=new BookingDates();

        dates.setCheckin( "2021-10-23");
        dates.setCheckout( "2021-10-24");
        requestEntityBuilder.setFirstname("kishore");
        requestEntityBuilder.setLastname("T");
        requestEntityBuilder.setTotalprice(161);
        requestEntityBuilder.setDepositpaid(true);
        requestEntityBuilder.setBookingdates(dates);
        requestEntityBuilder.setAdditionalneeds("lunch");
        return requestEntityBuilder;
    }
    
    // Put json file in the project location
    // Create pojo classes for the json file
    // With jackson data bind dependency create object for object mapper
    // with the method obj.readValue(file,Classname.class) assign it to the pojo object
    // Now pass this pojo object in the body(object)
    
    
    // Jackson data binder
    // Pojo class
    // json file
    
    public RequestEntityBuilder updaterequestSpec() throws ParseException, JsonParseException, JsonMappingException, IOException {
    	ObjectMapper obj=new ObjectMapper();
    	update=obj.readValue(new File("./Update.json"), RequestEntityBuilder.class);
    
    	
         //dates.setCheckin( "2021-10-26");
         return update;
     }

    public String authInfo(){
        return "{\n" +
                "    \"username\" : \"admin\",\n" +
                "    \"password\" : \"password123\"\n" +
                "}";
    }
    public OauthPojo authInfoPojo() throws JsonParseException, JsonMappingException, IOException{
    	ObjectMapper obj=new ObjectMapper();
    	OauthPojo oauth=obj.readValue(new File("./Oauth.json"), OauthPojo.class);
    	
        return oauth;
    }
}
