#Author: Mathumathi

Feature: Verfiy Delete users API

 Scenario: Create a user to test delete request
   Given I send a POST request to "/createusers" 
    | user_first_name | user_last_name | user_contact_number | user_email_id   | plotNumber | street | state  | country | zipCode |
    | Mathudele         | Bala            | 1999999990         | bala7@example.com| pl-14       | Elm St | NY     | USA     | 12345   |
   Then I should receive a response with status code 201
    And the user should be created 
    When I store the userId from the response


  Scenario Outline: Verify user list for DELETE requests
    Given Base URL is set
    When I send a "<method>" request to the "<endpoint>" with the stored user ID and auth type as "<Auth>"
    Then Validate "<status_code>" and other data validations for scenario name as "<scenario_name>"

    Examples: 
   | scenario_name   | method | endpoint  							| Auth 			| status_code | 
	|invalid userid	 	|DELETE   | /deleteuser/100000   | Basic      | 404				|
	|userid in string	|DELETE   |  /deleteuser/invalid  | Basic 		| 400					|
	|	empty userid			| DELETE  |   										| Basic			|	  400				|
	|	No Auth					| DELETE	| /deleteuser/{userId}  | No Auth		|    401			|
	| Different method	| POST		| /deleteuser/{userId}  | Basic			|  405				|
	|Different method		| PUT 		| /deleteuser/{userId}  | Basic 		| 405					|
	|Different method		|  GET		|  /deleteuser/{userId}  | Basic		|		405				|
	|valid userid 			|DELETE 	|  /deleteuser/{userId} | Basic      | 200        | 
			

