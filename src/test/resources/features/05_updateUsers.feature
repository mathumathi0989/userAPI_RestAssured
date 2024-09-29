#Author: Mathumathi Balakrishnan

Feature: Verify Update User API

 Scenario: Create a user to test update request
   Given I send a POST to "/createusers" 
    | user_first_name | user_last_name | user_contact_number | user_email_id   | plotNumber | street | state  | country | zipCode |
    | Mathudel         | Bala            | 6999999890         | bala10k@example.com| pl-14       | Elm St | NY     | USA     | 12345   |
   Then I should get a response with status code 201
    And user should be created 
    When store the userId from the response


 Scenario: Validate update user API with data from Excel
  Given I have the base URL with stored userID
  When I send a PUT request with user data from all Excel rows
  Then the response data should match the request data in excel
  
  Scenario: For Data Cleanup, Delete the user 
Given Base URL is set
When Send DELETE to "/deleteuser/{userId}" with stored user ID
Then Validate status code 200 and message "User is deleted successfully"
