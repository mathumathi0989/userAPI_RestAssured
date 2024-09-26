package stepdefinition;

import static io.restassured.RestAssured.authentication;
import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

import config.ConfigProperties;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.authentication.PreemptiveBasicAuthScheme;
import io.restassured.response.Response;


public class getalltest {

	private Response response;
	private ConfigProperties configProperties = new ConfigProperties();
	
    @Given("^Base URL is set$")
    public void base_url_is_set() {
     
    }

    @When("^I send a \"([^\"]*)\" request to the \"([^\"]*)\" with \"([^\"]*)\"$")
    public void i_send_a_request_to_the_endpoint_with_auth(String method, String endpoint, String authType) {
        if (authType.equals("Basic")) {
            PreemptiveBasicAuthScheme auth = new PreemptiveBasicAuthScheme();
            auth.setUserName(configProperties.getUsername());
            auth.setPassword(configProperties.getPassword());
            authentication = auth;
        }

        String fullEndpoint = baseURI + (endpoint == null ? "" : endpoint);
        if (method.equalsIgnoreCase("GET")) {
            response = given().when().get(fullEndpoint);
        }
        else if (method.equalsIgnoreCase("POST")) {
        		response = given().when().post(fullEndpoint);
        }
        else if (method.equalsIgnoreCase("PUT")) {
    		response = given().when().put(fullEndpoint);
    }
        else if (method.equalsIgnoreCase("DELETE")) {
    		response = given().when().delete(fullEndpoint);
    }
    }

    @Then("Validate {string} , {string} and other data validations")
    public void validate_and_other_data_validations(String statusCodeStr, String expStatusLine) {
    	
    	 String responseBody = response.asString();
         System.out.println("Response Body as String: " + responseBody);
         System.out.println("Actual Status Code in the response is : " + response.getStatusCode());
       //  System.out.println("Actual Status Line in the response is : " + response.getStatusLine());

        int statusCode = Integer.parseInt(statusCodeStr); 

        response.then().statusCode(statusCode);
        
        
        response.then().statusLine(containsString(expStatusLine));
         
           
        
        if (response.getStatusCode() == 200) {
            int userCount = response.jsonPath().getList("$").size();
            System.out.println("Number of users: " + userCount);
            
        } else {
          //  System.out.println("Failed to fetch users. Status Code: " + response.getStatusCode());
        }
    
       
        
    }
    

	
}
