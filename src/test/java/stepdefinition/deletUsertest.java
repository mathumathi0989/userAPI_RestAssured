package stepdefinition;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import java.util.List;
import java.util.Map;

import org.testng.Reporter;
import org.testng.asserts.SoftAssert;

import config.ConfigProperties;
import config.LoggerLoad;
import hooks.hooks;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;




public class deletUsertest {
	
	private RequestSpecification request; 
	private Response response;
	private static int storedUserId;
	private ConfigProperties config;
	private Map<String, String> userData; // Instance variable to store user data

	public deletUsertest() {
		config = new ConfigProperties();
	}

	@Given("I send a POST request to {string}")
	public void i_send_a_post_request_to(String endpoint, DataTable dataTable) {
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

	@Then("the user should be created")
	public void the_user_should_be_created() {
		// Validate the response with stored userData
		response.then().statusCode(201).body("user_first_name", equalTo(userData.get("user_first_name")))
				.body("user_last_name", equalTo(userData.get("user_last_name")));
		LoggerLoad.info("User created successfully");
	}

	@When("I store the userId from the response")
	public void i_store_the_userId_from_the_response() {
		// Extract and store the userId from the response
		storedUserId = response.jsonPath().getInt("user_id");
		System.out.println("User ID for the newly created user is " + storedUserId);
	}

	@Then("I should receive a response with status code {int}")
	public void i_should_receive_a_response_with_status_code(int statusCode) {
		String responseBody = response.asString();
		System.out.println("Response Body: " + responseBody);
		response.then().statusCode(statusCode);
		LoggerLoad.info("Validated status code " + statusCode);
	}

	@When("I send a {string} request to the {string} with the stored user ID and auth type as {string}")
	public void i_send_a_request_to_with_the_stored_user_id_and_auth_type(String method, String endpoint, String auth) {
	    // Replace {userId} placeholder in the endpoint with the stored user ID
	    String updatedEndpoint = endpoint.replace("{userId}", String.valueOf(storedUserId));
	    
	    // Initialize the request specifier based on the authentication type
	    if (auth.equalsIgnoreCase("Basic")) {
	        // Use Basic Authentication
	        request = given().auth().basic(config.getUsername(), config.getPassword())
	                        .contentType(ContentType.JSON).log().all();
	    } else if (auth.equalsIgnoreCase("No Auth")) {
	        // Use No Authentication
	        request = given().auth().none().contentType(ContentType.JSON).log().all();
	    } else {
	        throw new IllegalArgumentException("Invalid auth type: " + auth);
	    }

	    // Send the appropriate HTTP request based on the method
	    switch (method.toUpperCase()) {
	        case "DELETE":
	            response = request.when().delete(hooks.baseURI + updatedEndpoint).then().log().all().extract().response();
	            break;
	        case "POST":
	            response = request.when().post(hooks.baseURI + updatedEndpoint).then().log().all().extract().response();
	            break;
	        case "PUT":
	            response = request.when().put(hooks.baseURI + updatedEndpoint).then().log().all().extract().response();
	            break;
	        case "GET":
	            response = request.when().get(hooks.baseURI + updatedEndpoint).then().log().all().extract().response();
	            break;
	        default:
	            throw new IllegalArgumentException("Invalid method: " + method);
	    }
	}

	@Then("Validate {string} and other data validations for scenario name as {string}")
	public void validate_and_other_data_validations(String expectedStatusCodeStr, String scenarioName) {
	    SoftAssert softAssert = new SoftAssert(); // Initialize SoftAssert
	    int expectedStatusCode = Integer.parseInt(expectedStatusCodeStr);
	    int actualStatusCode = response.getStatusCode();

	    // Retrieve error status and error message from the response (if available)
	    String errorStatus = response.jsonPath().getString("error.status");  // Assuming the error status field in JSON
	    String errorMessage = response.jsonPath().getString("error.message"); // Assuming the error message field in JSON

	    // Check if the expected status code matches the actual status code
	    if (actualStatusCode == expectedStatusCode) {
	        response.then().statusCode(expectedStatusCode);
	        LoggerLoad.info("Scenario: " + scenarioName + " passed with status code " + actualStatusCode);
	        
	        if (expectedStatusCode == 200) {
	            System.out.println("User deleted successfully.");
	        }
	    } else {
	        // Build the failure message
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
	        System.out.println(failureMessage);

	        // Log failure in Logger
	        LoggerLoad.error("Scenario: " + scenarioName + " failed. Expected status code " 
	                + expectedStatusCode + " but received " + actualStatusCode);

	        // Soft assert failure to allow test to continue
	        softAssert.fail(failureMessage);
	    }

	    // At the end, assert all to check for any failures
	    softAssert.assertAll(); // Ensure this line is present
	}
}
