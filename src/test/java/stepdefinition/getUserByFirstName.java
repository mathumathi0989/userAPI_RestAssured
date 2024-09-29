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

public class getUserByFirstName {

	
	
		private RequestSpecification request; 
		private Response response;
		private static String storedUserFirstName;
		private ConfigProperties config;
		private Map<String, String> userData; 

		public getUserByFirstName() {
			config = new ConfigProperties();
		}

		@Given("Send POST to {string} to test GET user by firstName")
		public void send_post_to_to_test_get_user_by_firstname(String endpoint, DataTable dataTable) {
			// Convert the DataTable into a List of Maps
			List<Map<String, String>> dataList = dataTable.asMaps(String.class, String.class);

			if (!dataList.isEmpty()) {
				userData = dataList.get(0); 

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

		@Then("user got created with firstName")
		public void user_got_created_with_firstname() {
			response.then().statusCode(201).body("user_first_name", equalTo(userData.get("user_first_name")))
					.body("user_last_name", equalTo(userData.get("user_last_name")));
			LoggerLoad.info("User created successfully");
		}

		@When("store the response userFirstName")
		public void store_the_response_userFirstName() {
		
			storedUserFirstName = response.jsonPath().getString("user_first_name");
			System.out.println("User ID for the newly created user is " + storedUserFirstName);
		}

		@Then("validate response with status code {int} for get request")
		public void validate_response_with_status_code_for_get_request(int statusCode) {
			String responseBody = response.asString();
			System.out.println("Response Body: " + responseBody);
			response.then().statusCode(statusCode);
			LoggerLoad.info("Validated status code " + statusCode);
		}

		@When("I send a {string} request to {string} with stored user firstName and auth as {string}")
		public void i_send_a_request_to_with_stored_user_firstname_and_auth_as(String method, String endpoint, String auth) {
		    // Replace {userId} placeholder in the endpoint with the stored user ID
		    String updatedEndpoint = endpoint.replace("{userFirstName}", String.valueOf(storedUserFirstName));
		    
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

		@Then("Validate {string} and user validations for get request as {string}")
		public void validate_user_and_user_validations_for_get_request_as(String expectedStatusCodeStr, String scenarioName) {
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
		
		
		
		@When("I send a DELETE to {string} with the stored user firstName")
		public void i_send_a_delete_to_with_stored_user_firstname(String endpoint) {
		 
			response = given().auth().basic(config.getUsername(), config.getPassword()).pathParam("userfirstname", storedUserFirstName)
					.when().delete(hooks.baseURI + endpoint).then().extract().response();
		}
		
		
		@Then("Validate response with status code 200 and message {string}")
		public void Validate_response_with_status_code_and_message(String message) {
			response.then().statusCode(200).body("message", equalTo(message));
			LoggerLoad.info("validated status message");
		}

		
		
		
		
		
		
		
		
	

	
}
