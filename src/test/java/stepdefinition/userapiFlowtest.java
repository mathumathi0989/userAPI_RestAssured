package stepdefinition;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.testng.Assert.assertEquals;
import java.util.List;
import java.util.Map;

import org.testng.Assert;

import config.ConfigProperties;
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

	@Given("I send a GET request to {string}")
	public void i_send_a_get_request_to(String endpoint) {
		String fullurl = hooks.baseURI + endpoint;
		response = given().auth().basic(config.getUsername(), config.getPassword()).when().get(fullurl).then().extract()
				.response();
	}

	@Then("I should receive a valid response with status code {int}")
	public void i_should_receive_a_valid_response_with_status_code(int statusCode) {
		String responseBody = response.asString();
		System.out.println("Response Body as String: " + responseBody);
		response.then().statusCode(statusCode);
	}

	@Then("validate the number of users")
	public void validate_the_number_of_users() {
		initialUserCount = response.jsonPath().getList("users").size();
		System.out.println("Total number of users : " + initialUserCount);
		response.then().body("users.size()", greaterThan(0));

	}

	@When("I store the userId from the POST response")
	public void i_store_the_userId_from_the_post_response() {
		// Extract and store the userId from the response
		storedUserId = response.jsonPath().getInt("user_id");
		System.out.println("User id for the newly created user is " + storedUserId);
	}

	@When("I store the firstname from the PUT response")
	public void i_store_the_firstname_from_the_put_response() {

		storedFirstName = response.jsonPath().getString("user_first_name");
		System.out.println("User first name for the updated user is " + storedFirstName);

	}

	@Given("I send a POST request to {string} with the following data:")
	public void i_send_a_post_request_to_with_the_following_data(String endpoint, DataTable dataTable) {
		// Convert the DataTable into a List of Maps
		List<Map<String, String>> dataList = dataTable.asMaps(String.class, String.class);

		// Assuming there's only one row of data
		if (!dataList.isEmpty()) {
			userData = dataList.get(0);

			User user = new User();
			Address address = new Address();

			// Set user details
			user.setUser_first_name(userData.get("user_first_name"));
			user.setUser_last_name(userData.get("user_last_name"));
			user.setUser_contact_number(userData.get("user_contact_number"));
			user.setUser_email_id(userData.get("user_email_id"));

			// Set address details
			address.setPlotNumber(userData.get("plotNumber"));
			address.setStreet(userData.get("street"));
			address.setState(userData.get("state"));
			address.setCountry(userData.get("country"));
			address.setZipCode(userData.get("zipCode"));

			user.setUserAddress(address);

			response = given().auth().basic(config.getUsername(), config.getPassword()).contentType(ContentType.JSON)
					.log().all().body(user).when().post(hooks.baseURI + endpoint).then().log().all().extract()
					.response();

		}
	}

	@Then("the user should be created successfully")
	public void the_user_should_be_created_successfully() {
		response.then().statusCode(201).body("user_first_name", equalTo(userData.get("user_first_name")))
				.body("user_last_name", equalTo(userData.get("user_last_name")));

	}

	@Then("the user count should be increased by 1")
	public void the_user_count_should_be_increased_by_1() {
		newUserCount = given().auth().basic(config.getUsername(), config.getPassword()).when()
				.get(hooks.baseURI + "/users").then().extract().jsonPath().getList("users").size();
		System.out.println("Total number of user before creation " + initialUserCount);
		System.out.println("Total number of user after creation " + newUserCount);
		Assert.assertEquals(newUserCount, initialUserCount + 1);
	}

	@Given("I send a GET request to {string} with the stored user ID")
	public void i_send_a_get_request_with_stored_user_id(String endpoint) {

		response = given().auth().basic(config.getUsername(), config.getPassword()).pathParam("userId", storedUserId)
				.when().get(hooks.baseURI + endpoint).then().extract().response();
	}

	@Then("the response should contain the correct user details for the stored user ID")
	public void the_response_should_contain_the_correct_user_details_for_the_stored_user_ID() {
		// response.then().statusCode(200).body("userId", equalTo(userId));
		int responseUserId = response.jsonPath().getInt("user_id");
		assertEquals(storedUserId, responseUserId);

		String responseBody = response.asString();
		System.out.println("Response Body as String: " + responseBody);
	}

	@Given("I send a PUT request to {string} with user ID {string} and updated data:")
	public void i_send_a_put_request_to_with_user_id_and_updated_data(String endpoint, String userId,
			DataTable dataTable1) {
		List<Map<String, String>> dataList1 = dataTable1.asMaps(String.class, String.class);
		// Assuming there's only one row of data
		if (!dataList1.isEmpty()) {
			userData = dataList1.get(0);
		}

		User user = new User();
		Address address = new Address();

		// Set user details
		user.setUser_first_name(userData.get("up_user_first_name"));
		user.setUser_last_name(userData.get("up_user_last_name"));
		user.setUser_contact_number(userData.get("up_user_contact_number"));
		user.setUser_email_id(userData.get("up_user_email_id"));

		// Set address details
		address.setPlotNumber(userData.get("plotNumber"));
		address.setStreet(userData.get("street"));
		address.setState(userData.get("state"));
		address.setCountry(userData.get("country"));
		address.setZipCode(userData.get("zipCode"));

		user.setUserAddress(address);

		response = given().auth().basic(config.getUsername(), config.getPassword()).pathParam("userId", storedUserId)
				.contentType(ContentType.JSON).log().all().body(user).when().put(hooks.baseURI + endpoint).then().log()
				.all().extract().response();
	}

	@Then("the response should contain the updated user details")
	public void the_response_should_contain_the_updated_user_details() {
		response.then().statusCode(200).body("user_first_name", equalTo(userData.get("up_user_first_name")))
				.body("user_last_name", equalTo(userData.get("up_user_last_name")))
				.body("user_contact_number", equalTo(Long.valueOf(userData.get("up_user_contact_number"))))
				.body("user_email_id", equalTo(userData.get("up_user_email_id")));

	}

	@Given("I send a GET request to {string} with the stored user first name")
	public void i_send_a_get_request_to_with_the_stored_user_first_name(String endpoint) {
		response = given().auth().basic(config.getUsername(), config.getPassword())
				.pathParam("firstname", storedFirstName).log().all().when().get(hooks.baseURI + endpoint).then()
				.extract().response();
	}

	@Then("the response should contain the correct user details for the stored user first name")
	public void the_response_should_contain_the_correct_user_details_for_the_stored_user_first_name() {
		// response.then().statusCode(200).body("user_first_name", equalTo(firstName));
		String responseUserFirstName = response.jsonPath().getString("[0].user_first_name");
		System.out.println("check   responseUserFirstName " + responseUserFirstName);
		System.out.println("check    storedFirstName " + storedFirstName);
		assertEquals(storedFirstName, responseUserFirstName);
		String responseBody = response.asString();
		System.out.println("Response Body as String: " + responseBody);
	}

	@Given("I send a DELETE request to {string} with the stored user ID")
	public void i_send_a_delete_request_to_with_stored_user_id(String endpoint) {
		response = given().auth().basic(config.getUsername(), config.getPassword()).pathParam("userId", storedUserId)
				.when().delete(hooks.baseURI + endpoint).then().extract().response();
	}

	@Then("I should receive a valid response with status code 200 and message {string}")
	public void i_should_receive_a_valid_response_with_status_code_and_message(String message) {
		response.then().statusCode(200).body("message", equalTo(message));
	}

	@Then("the user count should be decreased by 1")
	public void the_user_count_should_be_decreased_by_1() {
		int updatedUserCount = given().auth().basic(config.getUsername(), config.getPassword()).when()
				.get(hooks.baseURI + "/users").then().extract().jsonPath().getList("users").size();
		System.out.println("Total number of user before deleting " + newUserCount);
		System.out.println("Total number of user after deleting " + updatedUserCount);
		Assert.assertEquals(updatedUserCount, newUserCount - 1);
	}
}
