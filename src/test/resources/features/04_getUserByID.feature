#Author: Mathumathi
@functional
Feature: Verfiy GET user by ID API

 Scenario: Create a user to test GET user by ID request
   Given Send POST to "/createusers" 
    | user_first_name | user_last_name | user_contact_number | user_email_id   | plotNumber | street | state  | country | zipCode |
    | Mathuget        | Bala            | 1999979090         | bala11k@example.com| pl-14       | Elm St | NY     | USA     | 12345   |
   Then validate response with status code 201
    And user got created
    When store the response userid


  Scenario Outline: Verify user detail with GET request by ID
    Given Base URL is set
    When I send a "<method>" request to "<endpoint>" with stored user ID and auth as "<Auth>"
    Then Validate "<status_code>" and other user validations for scenario name as "<scenario_name>"
    
    Examples: 
   | scenario_name   | method		 | endpoint  							| Auth 			| status_code | 
	|invalid userid	 	|GET  			 | /user/100000  					 | Basic      | 404				|
	|userid in string	|GET   			|  /user/invalid  				| Basic 		| 400					|
	|	empty userid		| GET  			|   											| Basic			|	  400				|
	|	No Auth					| GET				| /user/{userId}  				| No Auth		|    401			|
	| Different method	| POST		| /user/{userId}  				| Basic			|  405				|
	|Different method		| PUT 		| /user/{userId}  				| Basic 		| 405					|
	|Different method		|  DELETE		|  /user/{userId}  				| Basic			|		405				|
	|valid userid 			|GET 			|  /user/{userId} 				| Basic      | 200        | 
			

Scenario: For Get Request By ID data clean up, Delete the user 
Given Base URL is set
When I send a DELETE to "/deleteuser/{userId}" with the stored user ID
Then Validate response with status code 200 and message "User is deleted successfully" for get request


