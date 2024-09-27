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
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.qameta.allure.Allure;
import io.restassured.http.ContentType;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;

public class updateUsertest {

	private ConfigProperties config;
    private Response response;
    private SoftAssert softAssert = new SoftAssert(); // Initialize SoftAssert
	private static int storedUserId;
	
    // Extent Reports
    private static ExtentReports extentReports;
    private ExtentTest extentTest;

    // List to store user data from the PUT response
    private List<Map<String, String>> storedUserData;
    private Map<String, String> userData;

    public updateUsertest() {
        config = new ConfigProperties();
        storedUserData = new ArrayList<>();
        // Initialize ExtentReports
        ExtentHtmlReporter htmlReporter = new ExtentHtmlReporter("ExtentReports.html");
        extentReports = new ExtentReports();
        extentReports.attachReporter(htmlReporter);
    }

    @Given("I send a POST to {string}")
	public void i_send_a_post_to(String endpoint, DataTable dataTable) {
		// Convert the DataTable into a List of Maps
		List<Map<String, String>> dataList = dataTable.asMaps(String.class, String.class);

		// Assuming there's only one row of data
		if (!dataList.isEmpty()) {
			userData = dataList.get(0); // Store the user data in instance variable

			// Create JSON body directly from the user data map
			String requestBody = String.format(
					"{\"user_first_name\":\"%s\", \"user_last_name\":\"%s\", "
							+ "\"user_contact_number\":\"%s\", \"user_email_id\":\"%s\", "
							+ "\"userAddress\": {\"plotNumber\":\"%s\", \"street\":\"%s\", \"state\":\"%s\", "
							+ "\"country\":\"%s\", \"zipCode\":\"%s\"}}",
					userData.get("user_first_name"), userData.get("user_last_name"),
					userData.get("user_contact_number"), userData.get("user_email_id"), userData.get("plotNumber"),
					userData.get("street"), userData.get("state"), userData.get("country"), userData.get("zipCode"));

			// Send POST request
			response = given().auth().basic(config.getUsername(), config.getPassword()).contentType(ContentType.JSON)
					.log().all().body(requestBody).when().post(hooks.baseURI + endpoint).then().log().all().extract()
					.response();
		}
	}

	@Then("user should be created")
	public void the_user_should_be_created() {
		// Validate the response with stored userData
		response.then().statusCode(201).body("user_first_name", equalTo(userData.get("user_first_name")))
				.body("user_last_name", equalTo(userData.get("user_last_name")));
		LoggerLoad.info("User created successfully");
	}

	@When("store the userId from the response")
	public void store_the_userId_from_the_response() {
		// Extract and store the userId from the response
		storedUserId = response.jsonPath().getInt("user_id");
		System.out.println("User ID for the newly created user is " + storedUserId);
	}

	@Then("I should get a response with status code {int}")
	public void i_should_get_a_response_with_status_code(int statusCode) {
		String responseBody = response.asString();
		System.out.println("Response Body: " + responseBody);
		response.then().statusCode(statusCode);
		LoggerLoad.info("Validated status code " + statusCode);
	}

	
    @Given("I have the base URL with stored userID")
    public void i_have_the_base_url_with_stored_userID() {
        // Base URL is handled via config or hooks; no setup needed here.
    }

    @When("I send a PUT request with user data from all Excel rows")
    public void i_send_a_put_request_with_user_data_from_all_excel_rows() {
        // Fetch data from Excel file for all rows
        String filePath = "src/test/resources/excelData/UpdateData.xlsx";
        List<Map<String, String>> allData = ExcelUtil.getAllExcelData(filePath, "PUT");

        for (Map<String, String> data : allData) {
            // Execute scenario for each row
            executeScenario(data);
            printStoredUserData();
        }
    }

    private void executeScenario(Map<String, String> data) {
    	  // Log the scenario name from the Excel column
        String scenarioName = data.get("scenario_name");
        System.out.println("Starting scenario: " + scenarioName);

        // Create an ExtentTest for the scenario
        extentTest = extentReports.createTest(scenarioName);

        // Use storedUserId instead of fetching from Excel
        if (storedUserId == 0) {
            String failureMessage = "No stored userId. Please ensure a user is created before this step.";
            extentTest.fail(failureMessage);
            throw new RuntimeException(failureMessage);
        }

        // Use data to form the request body for updating user details
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

        // Construct the PUT request URL using storedUserId
        String fullUrl = hooks.baseURI + "/updateuser/" + storedUserId;

        // Send PUT request and handle response
        response = given()
                .auth().basic(config.getUsername(), config.getPassword())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .log().all()
                .put(fullUrl)
                .then()
                .log().all()
                .extract().response();

        // Validate the status code and response data
        validateResponse(data, scenarioName, storedUserId);
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
 }

    private void validateResponse(Map<String, String> data, String scenarioName, int storedUserId) {
        // Check expected status code
        int expectedStatusCode = Integer.parseInt(data.get("status_code"));
        int actualStatusCode = response.statusCode();

        // Extract status and message from the response body (if present)
        String errorStatus = response.jsonPath().getString("status");
        String errorMessage = response.jsonPath().getString("message");

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

            // Additional validation if status is 200 (user updated successfully)
            if (actualStatusCode == 200 && storedUserId != 0) {
                // Validate user details from the response
                String returnedUserId = response.jsonPath().getString("user_id");
                if (Integer.parseInt(returnedUserId) == storedUserId) {
                    System.out.println("User updated successfully. User ID: " + returnedUserId);
                } else {
                    String failureMessage = "Mismatch in returned user_id. Expected: " + storedUserId 
                            + " but got: " + returnedUserId;
                    softAssert.fail(failureMessage);
                    extentTest.fail(failureMessage);
                }

                storeUserData(response);
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

    @Then("the response data should match the request data in excel")
    public void the_response_data_should_match_the_request_data_in_excel() {
        // Compare actual response data with expected request data
        String actualFirstName = response.jsonPath().getString("user_first_name");
        response.then().body("user_first_name", equalTo(actualFirstName));
        String actualLastName = response.jsonPath().getString("user_last_name");
        response.then().body("user_last_name", equalTo(actualLastName));
        String actualEmailID = response.jsonPath().getString("user_email_id");
        response.then().body("user_email_id", equalTo(actualEmailID));
    }

    @After
    public void tearDown() {
        // Ensure all soft assertions are checked
        softAssert.assertAll();

        // Flush the ExtentReports at the end of the test suite
        extentReports.flush();
    }
	
}
