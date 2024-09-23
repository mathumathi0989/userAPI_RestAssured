package stepdefinition;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class sampletest {

	private String endpoint;
    private Response response;

    @Given("I have the API endpoint")
    public void i_have_the_api_endpoint() {
        endpoint = "https://jsonplaceholder.typicode.com/posts/1"; // Sample API
        System.out.println("api base url");
    }

    @When("I send a GET request to the API")
    public void i_send_a_get_request_to_the_api() {
        response = RestAssured.get(endpoint);
        System.out.println("endpoint provided");
    }

    @Then("I should receive a successful response")
    public void i_should_receive_a_successful_response() {
        // Validate the response status code
        response.then().statusCode(200);
      System.out.println("validated the status");
    }


	
	
}
