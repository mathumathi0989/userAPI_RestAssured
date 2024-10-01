#Author: Mathumathi Balakrishnan
Feature: User API flow To test endpoints for creating, fetching, updating, and deleting users
  
  Scenario: Get All Users
    Given I send a GET request to "/users"
    Then validate the number of users and status code 200
  
  
  Scenario: Create User
   Given I send a POST request to "/createusers" with the following data:
    | user_first_name | user_last_name | user_contact_number | user_email_id   | plotNumber | street | state  | country | zipCode |
    | MathuTr          | Bala            | 1999969890         | bala3@example.com| pl-14       | Elm St | NY     | USA     | 12345   |
   Then validate the user details with status code 201 and store the userId
   
  
  Scenario: Get User by ID
    Given I send a GET request to "/user/{userId}" with the stored user ID
    Then verify the user details and validate response with status code 200
    
  
  Scenario: Update User by ID
    Given I send a PUT request to "/updateuser/{userId}" with user ID "<userId>" and updated data:
      | up_user_first_name   | up_user_last_name   | up_user_contact_number | up_user_email_id        |plotNumber | street   | state   | country | zipCode |
      | Mathuupdat            | Balak               | 2987633377           | jane.smith@mail.com         |pl-39      | Broadway | NY    | USA       | 19001  |
    Then validate the updated user details with status code 200 and store the firstname
  
  
  Scenario: Get User by First Name
    Given I send a GET request to "/users/username/{firstname}" with the stored user first name
    Then validate the updated user details for the stored user firstname and verify response with status code 200
 
  
  Scenario: Delete User by ID
    Given I send a DELETE request to "/deleteuser/{userId}" with the stored user ID
    Then Validate the user count and response with status code 200 and message "User is deleted successfully"
    
