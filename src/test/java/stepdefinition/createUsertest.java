package stepdefinition;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Reporter;
import org.testng.asserts.SoftAssert;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;

import config.ConfigProperties;
import config.ExcelUtil;
import config.LoggerLoad;
import hooks.hooks;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.qameta.allure.Allure;
import io.restassured.http.ContentType;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;

public class createUsertest {

    private ConfigProperties config;
    private Response response;
    private SoftAssert softAssert = new SoftAssert(); // Initialize SoftAssert
    private String userId;
    // Extent Reports
    private static ExtentReports extentReports;
    private ExtentTest extentTest;

    // List to store user data from the POST response
    private List<Map<String, String>> storedUserData;

    public createUsertest() {
        config = new ConfigProperties();
        storedUserData = new ArrayList<>();
        // Initialize ExtentReports
        ExtentHtmlReporter htmlReporter = new ExtentHtmlReporter("ExtentReports.html");
        extentReports = new ExtentReports();
        extentReports.attachReporter(htmlReporter);
    }

    @Given("I have the base URL")
    public void i_have_the_base_url() {
        // This can remain empty or contain setup code for base URL if necessary
    }

    @When("I send a POST request with user data from all Excel rows")
    public void i_send_a_post_request_with_user_data_from_all_excel_rows() {
        // Fetch data from Excel file for all rows
        String filePath = "src/test/resources/excelData/InputData.xlsx";
        List<Map<String, String>> allData = ExcelUtil.getAllExcelData(filePath, "POST");

        for (Map<String, String> data : allData) {
            // Execute scenario for each row
            executeScenario(data);
            printStoredUserData();
        }
    }

    private void executeScenario(Map<String, String> data) {
        // Log the scenario name from the Excel column
        String scenarioName = data.get("scenario_name");
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!" + "Starting scenario: " + scenarioName + "!!!!!!!!!!!!!!!!!!!!!!!!!!");

        // Create an ExtentTest for the scenario
        extentTest = extentReports.createTest(scenarioName);
        
        // Use data to form the request body
        String requestBody = "{\n" +
                "\"user_first_name\": \"" + data.get("user_first_name") + "\",\n" +
                "\"user_last_name\": \"" + data.get("user_last_name") + "\",\n" +
                "\"user_contact_number\": \"" + data.get("user_contact_number") + "\",\n" +
                "\"user_email_id\": \"" + data.get("user_email_id") + "\",\n" +
                "\"userAddress\": {\n" +
                "\"plotNumber\": \"" + data.get("plotNumber") + "\",\n" +
                "\"street\": \"" + data.get("street") + "\",\n" +
                "\"state\": \"" + data.get("state") + "\",\n" +
                "\"country\": \"" + data.get("country") + "\",\n" +
                "\"zipCode\": \"" + data.get("zipCode") + "\"\n" +
                "}\n" +
                "}";

        String fullUrl = hooks.baseURI + data.get("endpoint");

        // Send POST request and handle response
        response = given()
                .auth().basic(config.getUsername(), config.getPassword())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .log().all()
                .post(fullUrl)
                .then()
                .log().all()
                .extract().response();

        // Validate the status code and response data
        validateResponse(data, scenarioName);
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
    }

    private void validateResponse(Map<String, String> data, String scenarioName) {
    	  // Check expected status code
        int expectedStatusCode = Integer.parseInt(data.get("status_code"));
        int actualStatusCode = response.statusCode();

        // Extract status and message from the response body (if present)
        String errorStatus = response.jsonPath().getString("status");
        String errorMessage = response.jsonPath().getString("message");

        // Print the actual status code and indicate if it does not match the expected status code
        if (actualStatusCode != expectedStatusCode) {
            String failureMessage = "Failure in scenario: " + scenarioName 
                + " | Expected status code: " + expectedStatusCode 
                + " but got: " + actualStatusCode;

            // Append error status and error message if they are not null or empty
            if (errorStatus != null && !errorStatus.isEmpty()) {
                failureMessage += " | Error Status: " + errorStatus;
            }
            if (errorMessage != null && !errorMessage.isEmpty()) {
                failureMessage += " | Error Message: " + errorMessage;
            }

            // Log failure in the report and console
            Reporter.log(failureMessage);
            Allure.addAttachment("Failure Message", failureMessage); // Attach failure message to Allure
            
         // Important: Log failure to ExtentReports
            if (extentTest != null) {
                extentTest.fail(failureMessage); // Log to ExtentReports
            }
            
            System.out.println(failureMessage);

            // Mark the test as failed in TestNG
            softAssert.fail(failureMessage);
        } else {
        	 String successMessage = "Success: Status code matched the expected value: " + expectedStatusCode;
        	 // Log success in the ExtentReports
             if (extentTest != null) {
                 extentTest.pass(successMessage);
             }
             Allure.addAttachment("Success Message", successMessage); // Attach success message to Allure
             
             System.out.println(successMessage);
            if (response.statusCode() == 201) {
                 userId = response.jsonPath().getString("user_id");
                response.then().body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/createUserSchema.json"));
                System.out.println("------------------------- User ID created: " + userId + " ---------------------------");
                storeUserData(response);
              //  if (userId == null || userId.isEmpty()) {
        	    //    throw new IllegalArgumentException("userId cannot be null or empty");
        	    //}
        	    
        	    System.out.println("Attempting to delete user with ID: " + userId);
        	    System.out.println("DELETE endpoint: " + hooks.baseURI + "/deleteuser/{userId}");

        	    response = given()
        	        .auth().basic(config.getUsername(), config.getPassword())
        	        .pathParam("userId", userId)
        	        .when()
        	        .delete(hooks.baseURI + "/deleteuser/{userId}")
        	        .then()
        	        .extract().response();
        	    response.then().statusCode(200).body("message", equalTo("User is deleted successfully"));
        		LoggerLoad.info("validated status message");
    	
    	
            }
        }
    }

    private void storeUserData(Response response) {
        // Extract user details from the response
        String userId = response.jsonPath().getString("user_id");
        String firstName = response.jsonPath().getString("user_first_name");
        String lastName = response.jsonPath().getString("user_last_name");
        String contactNumber = response.jsonPath().getString("user_contact_number");
        String emailId = response.jsonPath().getString("user_email_id");

        // Create a map to store the user data
        Map<String, String> userData = new HashMap<>();
        userData.put("user_id", userId);
        userData.put("user_first_name", firstName);
        userData.put("user_last_name", lastName);
        userData.put("user_contact_number", contactNumber);
        userData.put("user_email_id", emailId);

        // Add the user data to the list
        storedUserData.add(userData);
    }

    private void printStoredUserData() {
        System.out.println("Stored User Data:");
        for (Map<String, String> userData : storedUserData) {
            String StoredUserId = userData.get("user_id");
            String StoredFirstName = userData.get("user_first_name");
            String StoredLastName = userData.get("user_last_name");
            String StoredContactNumber = userData.get("user_contact_number");
            String StoredEmailId = userData.get("user_email_id");

            System.out.println("Stored User ID: " + StoredUserId);
            System.out.println("Stored First Name: " + StoredFirstName);
            System.out.println("Stored Last Name: " + StoredLastName);
            System.out.println("Stored Contact Number: " + StoredContactNumber);
            System.out.println("Stored Email ID: " + StoredEmailId);
            System.out.println("--------------------------------");
        }
    }

    @Then("the response data should match the request from all Excel rows")
    public void the_response_data_should_match_the_request_from_all_excel_rows() {
        String actualFirstName = response.jsonPath().getString("user_first_name");
        response.then().body("user_first_name", equalTo(actualFirstName));
        String actualLastName = response.jsonPath().getString("user_last_name");
        response.then().body("user_last_name", equalTo(actualLastName));
        String actualEmailID = response.jsonPath().getString("user_email_id");
        response.then().body("user_email_id", equalTo(actualEmailID));
    }
    
    @After
    public void tearDown() {
    	 softAssert.assertAll();
        // Flush the ExtentReports at the end of the test suite
        extentReports.flush();
    }
    
  	
	


	
  
}
