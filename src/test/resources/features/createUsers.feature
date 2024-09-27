#Author: Mathumathi Balakrishnan

Feature: Create User API

 Scenario: Validate create user API with data from Excel
  Given I have the base URL
  When I send a POST request with user data from all Excel rows
  Then the response data should match the request from all Excel rows