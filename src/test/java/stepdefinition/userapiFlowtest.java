package stepdefinition;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.testng.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.testng.Assert;

import config.ConfigProperties;
import config.LoggerLoad;
import hooks.hooks;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import pojo.Address;
import pojo.User;

public class userapiFlowtest {

	private Response response;
	private static int storedUserId;
	private static String storedFirstName;

	public static int initialUserCount;
	public static int newUserCount;
	private ConfigProperties config;

	private Map<String, String> userData;

	public userapiFlowtest() {
		config = new ConfigProperties();
	}

	  // Helper method to send GET request
    private Response sendGetRequest(String endpoint) {
        return given().auth().basic(config.getUsername(), config.getPassword())
                      .when().get(hooks.baseURI + endpoint)
                      .then().extract().response();
    }

    // Helper method to send POST request
    private Response sendPostRequest(String endpoint, User user) {
        return given().auth().basic(config.getUsername(), config.getPassword())
                      .contentType(ContentType.JSON)
                      .log().all().body(user)
                      .when().post(hooks.baseURI + endpoint)
                      .then().log().all().extract().response();
    }

    // Helper method to send PUT request
    private Response sendPutRequest(String endpoint, User user) {
        return given().auth().basic(config.getUsername(), config.getPassword())
                      .contentType(ContentType.JSON)
                      .log().all().body(user)
                      .when().put(hooks.baseURI + endpoint)
                      .then().log().all().extract().response();
    }

    // Helper method to validate response status code and user details
    private void validateUserDetails(Response response, String firstName, String lastName) {
        response.then().statusCode(200)
                .body("user_first_name", equalTo(firstName))
                .body("user_last_name", equalTo(lastName));
        LoggerLoad.info("Validated user details");
    }

    // Helper method to create User from DataTable
    private User createUserFromDataTable(DataTable dataTable) {
        List<Map<String, String>> dataList = dataTable.asMaps(String.class, String.class);

        if (!dataList.isEmpty()) {
            userData = dataList.get(0);
            User user = new User();
            Address address = new Address();

            user.setUser_first_name(userData.get("user_first_name"));
            user.setUser_last_name(userData.get("user_last_name"));
            user.setUser_contact_number(userData.get("user_contact_number"));
            user.setUser_email_id(userData.get("user_email_id"));

            address.setPlotNumber(userData.get("plotNumber"));
            address.setStreet(userData.get("street"));
            address.setState(userData.get("state"));
            address.setCountry(userData.get("country"));
            address.setZipCode(userData.get("zipCode"));

            user.setUserAddress(address);

            return user;
        }
        return null;
    }

    @Given("I send a GET request to {string}")
    public void i_send_a_get_request_to(String endpoint) {
        response = sendGetRequest(endpoint);
    }

    @Then("validate the number of users and status code {int}")
    public void validate_the_number_of_users_and_status_code(int statusCode) {
        response.then().statusCode(statusCode);
        LoggerLoad.info("Validated status code");
        initialUserCount = response.jsonPath().getList("users").size();
        LoggerLoad.info("Validated users count");
    }

    @Given("I send a POST request to {string} with the following data:")
    public void i_send_a_post_request_to_with_the_following_data(String endpoint, DataTable dataTable) {
        User user = createUserFromDataTable(dataTable);
        if (user != null) {
            response = sendPostRequest(endpoint, user);
        }
    }

    @Then("validate the user details with status code {int} and store the userId")
    public void validate_the_user_details_with_status_code_and_store_the_userId(int statusCode) {
        response.then().statusCode(statusCode);
        validateUserDetails(response, userData.get("user_first_name"), userData.get("user_last_name"));
        storedUserId = response.jsonPath().getInt("user_id");
        LoggerLoad.info("Stored user ID: " + storedUserId);
    }

    @Given("I send a PUT request to {string} with user ID {string} and updated data:")
    public void i_send_a_put_request_to_with_user_id_and_updated_data(String endpoint, String userId, DataTable dataTable) {
        User user = createUserFromDataTable(dataTable);
        if (user != null) {
            response = sendPutRequest(endpoint, user);
        }
    }

    @Then("validate the updated user details with status code {int} and store the firstname")
    public void validate_the_updated_user_details_with_status_code_and_store_the_firstname(int statusCode) {
        response.then().statusCode(statusCode);
        validateUserDetails(response, userData.get("up_user_first_name"), userData.get("up_user_last_name"));
        storedFirstName = response.jsonPath().getString("user_first_name");
        LoggerLoad.info("Stored first name: " + storedFirstName);
    }

    @Given("I send a GET request to {string} with the stored user ID")
    public void i_send_a_get_request_to_with_the_stored_user_id(String endpoint) {
    	response = sendGetRequest(endpoint + "?id=" + storedUserId); 
    }
    @Then("verify the user details and validate response with status code {int}")
    public void verify_the_user_details_and_validate_response_with_status_code(Integer expectedStatusCode) {
    	 // Validate status code
        Assert.assertEquals(expectedStatusCode.intValue(), response.getStatusCode());

        // Verify user details in the response
        String actualUserId = response.jsonPath().getString("id");  // Assuming the user ID is returned in the 'id' field
        Assert.assertEquals(storedUserId, actualUserId);
    }
    
    @Given("I send a GET request to {string} with the stored user first name")
    public void i_send_a_get_request_to_with_the_stored_user_first_name(String endpoint) {
    	 response = sendGetRequest(endpoint + "?firstName=" + storedFirstName); // Sending first name as a query parameter

    }
    @Then("validate the updated user details for the stored user firstname and verify response with status code {int}")
    public void validate_the_updated_user_details_for_the_stored_user_firstname_and_verify_response_with_status_code(Integer expectedStatusCode) {
    	 Assert.assertEquals(expectedStatusCode.intValue(), response.getStatusCode());

         // Verify the user first name in the response
         String actualFirstName = response.jsonPath().getString("firstName");  // Assuming the first name is returned in the 'firstName' field
         Assert.assertEquals(storedFirstName, actualFirstName);
    }
    
    @Given("I send a DELETE request to {string} with the stored user ID")
    public void i_send_a_delete_request_to_with_stored_user_id(String endpoint) {
        response = given().auth().basic(config.getUsername(), config.getPassword())
                           .pathParam("userId", storedUserId)
                           .when().delete(hooks.baseURI + endpoint)
                           .then().extract().response();
    }

    @Then("Validate the user count and response with status code 200 and message {string}")
    public void Validate_the_user_count_and_response_with_status_code_200_and_message(String message) {
        response.then().statusCode(200).body("message", equalTo(message));
        LoggerLoad.info("Validated status message");
        int updatedUserCount = given().auth().basic(config.getUsername(), config.getPassword())
                                      .when().get(hooks.baseURI + "/users")
                                      .then().extract().jsonPath().getList("users").size();
        Assert.assertEquals(updatedUserCount, newUserCount - 1);
        LoggerLoad.info("User count decreased by 1 after deletion");
    }
	
	
//	@Given("I send a GET request to {string}")
//	public void i_send_a_get_request_to(String endpoint) {
//		String fullurl = hooks.baseURI + endpoint;
//		response = given().auth().basic(config.getUsername(), config.getPassword()).when().get(fullurl).then().extract()
//				.response();
//	}
//
//	
//	
//	@Then("validate the number of users and status code {int}")
//	public void validate_the_number_of_users_and_status_code(int statusCode) {
//		String responseBody = response.asString();
//		System.out.println("Response Body as String: " + responseBody);
//		response.then().statusCode(statusCode);
//		LoggerLoad.info("validated status code");
//		initialUserCount = response.jsonPath().getList("users").size();
//		System.out.println("Total number of users : " + initialUserCount);
//		response.then().body("users.size()", greaterThan(0));
//		LoggerLoad.info("validated users count");
//
//	}
//
//
//
//	@Given("I send a POST request to {string} with the following data:")
//	public void i_send_a_post_request_to_with_the_following_data(String endpoint, DataTable dataTable) {
//	
//		List<Map<String, String>> dataList = dataTable.asMaps(String.class, String.class);
//
//		if (!dataList.isEmpty()) {
//			userData = dataList.get(0);
//
//			User user = new User();
//			Address address = new Address();
//
//			user.setUser_first_name(userData.get("user_first_name"));
//			user.setUser_last_name(userData.get("user_last_name"));
//			user.setUser_contact_number(userData.get("user_contact_number"));
//			user.setUser_email_id(userData.get("user_email_id"));
//
//	
//			address.setPlotNumber(userData.get("plotNumber"));
//			address.setStreet(userData.get("street"));
//			address.setState(userData.get("state"));
//			address.setCountry(userData.get("country"));
//			address.setZipCode(userData.get("zipCode"));
//
//			user.setUserAddress(address);
//
//			response = given().auth().basic(config.getUsername(), config.getPassword()).contentType(ContentType.JSON)
//					.log().all().body(user).when().post(hooks.baseURI + endpoint).then().log().all().extract()
//					.response();
//
//		}
//	}
//
//	
//	
//	@Then("validate the user details with status code {int} and store the userId")
//	public void validate_the_user_details_with_status_code_and_store_the_userId (int statusCode) {
//		response.then().statusCode(201).body("user_first_name", equalTo(userData.get("user_first_name")))
//		.body("user_last_name", equalTo(userData.get("user_last_name")));
//LoggerLoad.info("user created successfully");
//newUserCount = given().auth().basic(config.getUsername(), config.getPassword()).when()
//.get(hooks.baseURI + "/users").then().extract().jsonPath().getList("users").size();
//System.out.println("Total number of user before creation " + initialUserCount);
//System.out.println("Total number of user after creation " + newUserCount);
//Assert.assertEquals(newUserCount, initialUserCount + 1);
//LoggerLoad.info("After creation of user, total users count increase by 1");
//
//		String responseBody = response.asString();
//		System.out.println("Response Body as String: " + responseBody);
//		response.then().statusCode(statusCode);
//		LoggerLoad.info("validated status code");
//		storedUserId = response.jsonPath().getInt("user_id");
//		System.out.println("User id for the newly created user is " + storedUserId);
//
//	}
//
//	
//	
//	@Given("I send a GET request to {string} with the stored user ID")
//	public void i_send_a_get_request_with_stored_user_id(String endpoint) {
//
//		response = given().auth().basic(config.getUsername(), config.getPassword()).pathParam("userId", storedUserId)
//				.when().get(hooks.baseURI + endpoint).then().extract().response();
//	}
//
//	 
//	
//	@Then("verify the user details and validate response with status code {int}")
//	public void verify_the_user_details_and_validate_response_with_status_code(int statusCode) {
//		String responseBody = response.asString();
//		System.out.println("Response Body as String: " + responseBody);
//		response.then().statusCode(statusCode);
//		LoggerLoad.info("validated status code");
//		
//		int responseUserId = response.jsonPath().getInt("user_id");
//		assertEquals(storedUserId, responseUserId);
//
//		System.out.println("Response Body as String: " + responseBody);
//		LoggerLoad.info("Validated user details for the created userID");
//	}
//
//	@Given("I send a PUT request to {string} with user ID {string} and updated data:")
//	public void i_send_a_put_request_to_with_user_id_and_updated_data(String endpoint, String userId,
//			DataTable dataTable1) {
//		List<Map<String, String>> dataList1 = dataTable1.asMaps(String.class, String.class);
//		if (!dataList1.isEmpty()) {
//			userData = dataList1.get(0);
//		}
//
//		User user = new User();
//		Address address = new Address();
//
//		user.setUser_first_name(userData.get("up_user_first_name"));
//		user.setUser_last_name(userData.get("up_user_last_name"));
//		user.setUser_contact_number(userData.get("up_user_contact_number"));
//		user.setUser_email_id(userData.get("up_user_email_id"));
//
//		address.setPlotNumber(userData.get("plotNumber"));
//		address.setStreet(userData.get("street"));
//		address.setState(userData.get("state"));
//		address.setCountry(userData.get("country"));
//		address.setZipCode(userData.get("zipCode"));
//
//		user.setUserAddress(address);
//
//		response = given().auth().basic(config.getUsername(), config.getPassword()).pathParam("userId", storedUserId)
//				.contentType(ContentType.JSON).log().all().body(user).when().put(hooks.baseURI + endpoint).then().log()
//				.all().extract().response();
//	}
//
//		
//	@Then("validate the updated user details with status code {int} and store the firstname")
//	public void validate_the_updated_user_details_with_status_code_and_store_the_firstname(int statusCode) {
//	
//		String responseBody = response.asString();
//		System.out.println("Response Body as String: " + responseBody);
//		response.then().statusCode(statusCode);
//		LoggerLoad.info("validated status code");
//		
//		response.then().statusCode(200).body("user_first_name", equalTo(userData.get("up_user_first_name")))
//				.body("user_last_name", equalTo(userData.get("up_user_last_name")))
//				.body("user_contact_number", equalTo(Long.valueOf(userData.get("up_user_contact_number"))))
//				.body("user_email_id", equalTo(userData.get("up_user_email_id")));
//		LoggerLoad.info("Validated user details for the updated userID");
//		storedFirstName = response.jsonPath().getString("user_first_name");
//		System.out.println("User first name for the updated user is " + storedFirstName);
//
//	}
//	
//	
//	@Given("I send a GET request to {string} with the stored user first name")
//	public void i_send_a_get_request_to_with_the_stored_user_first_name(String endpoint) {
//		response = given().auth().basic(config.getUsername(), config.getPassword())
//				.pathParam("firstname", storedFirstName).log().all().when().get(hooks.baseURI + endpoint).then()
//				.extract().response();
//	}
//
//	
//	
//	
//	@Then("validate the updated user details for the stored user firstname and verify response with status code {int}")
//	public void validate_the_updated_user_details_for_the_stored_user_firstname_and_verify_response_with_status_code(int statusCode) {
//		String responseBody = response.asString();
//		System.out.println("Response Body as String: " + responseBody);
//		response.then().statusCode(statusCode);
//		LoggerLoad.info("validated status code");
//		
//		String responseUserFirstName = response.jsonPath().getString("[0].user_first_name");
//		System.out.println("check   responseUserFirstName " + responseUserFirstName);
//		System.out.println("check    storedFirstName " + storedFirstName);
//		assertEquals(storedFirstName, responseUserFirstName);
//		
//		System.out.println("Response Body as String: " + responseBody);
//		LoggerLoad.info("Validated user details for the stored user first name");
//	}
//
//	@Given("I send a DELETE request to {string} with the stored user ID")
//	public void i_send_a_delete_request_to_with_stored_user_id(String endpoint) {
//		response = given().auth().basic(config.getUsername(), config.getPassword()).pathParam("userId", storedUserId)
//				.when().delete(hooks.baseURI + endpoint).then().extract().response();
//	}
//
//	
//	
//	@Then("Validate the user count and response with status code 200 and message {string}")
//	public void Validate_the_user_count_and_response_with_status_code_200_and_message(String message) {
//		response.then().statusCode(200).body("message", equalTo(message));
//		LoggerLoad.info("validated status message");
//		int updatedUserCount = given().auth().basic(config.getUsername(), config.getPassword()).when()
//				.get(hooks.baseURI + "/users").then().extract().jsonPath().getList("users").size();
//		System.out.println("Total number of user before deleting " + newUserCount);
//		System.out.println("Total number of user after deleting " + updatedUserCount);
//		Assert.assertEquals(updatedUserCount, newUserCount - 1);
//		LoggerLoad.info("After deletion of user, total users count decreased by 1");
//	}

	
}
