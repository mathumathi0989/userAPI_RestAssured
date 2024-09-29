#Author: Mathumathi

Feature: Verfiy get user by FirstName API

 Scenario: Create a user to test get user by FirstName request
   Given Send POST to "/createusers" to test GET user by firstName
    | user_first_name | user_last_name | user_contact_number | user_email_id   | plotNumber | street | state  | country | zipCode |
    | Mathugets        | Bala            | 1999999095         | bala6@example.com| pl-14       | Elm St | NY     | USA     | 12345   |
   Then validate response with status code 201 for get request
    And user got created with firstName
    When store the response userFirstName


  Scenario Outline: Verify user detail with GET request by firstName
    Given Base URL is set
    When I send a "<method>" request to "<endpoint>" with stored user firstName and auth as "<Auth>"
    Then Validate "<status_code>" and user validations for get request as "<scenario_name>"
    
    Examples: 
   | scenario_name   				| method		 | endpoint  													| Auth 			| status_code | 
	|invalid firstName	 			|GET  			 | /user/username/alphabt  					 | Basic      | 404				|
	|firstName in integer			|GET   			|  /user/username/12633  								| Basic 		| 400					|
	|	empty firstName					| GET  			|   																		| Basic			|	  400				|
	|	No Auth									| GET				| /users/username/{userFirstName}  				| No Auth		|    401			|
	| Different method POST		| POST			| /users/username/{userFirstName} 				| Basic			|  405				|
	|Different method	PUT			| PUT 			| /users/username/{userFirstName} 				| Basic 		| 405					|
	|Different method	DELETE	|  DELETE		|  /users/username/{userFirstName}  				| Basic			|		405				|
	|valid firstName 					|GET 				|  /users/username/{userFirstName} 				| Basic      | 200        | 
			

Scenario: For Get Request By FirstName data clean up, Delete the user 
Given Base URL is set
When I send a DELETE to "/deleteuser/username/{userfirstname}" with the stored user firstName
Then Validate response with status code 200 and message "User is deleted successfully"


