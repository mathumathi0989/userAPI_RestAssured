Feature: Sample API Testing

  Scenario: Verify API status
    Given I have the API endpoint
    When I send a GET request to the API
    Then I should receive a successful response