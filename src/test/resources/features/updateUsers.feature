#Author: Mathumathi Balakrishnan

Feature: Update User API

 Scenario: Create User to test update scenario
   Given I send a POST to "/createusers" 
    | user_first_name | user_last_name | user_contact_number | user_email_id   | plotNumber | street | state  | country | zipCode |
    | Mathudel         | Bala            | 1999999890         | bala4@example.com| pl-14       | Elm St | NY     | USA     | 12345   |
   Then I should get a response with status code 201
    And user should be created 
    When store the userId from the response


 Scenario: Validate update user API with data from Excel
  Given I have the base URL with stored userID
  When I send a PUT request with user data from all Excel rows
  Then the response data should match the request data in excel