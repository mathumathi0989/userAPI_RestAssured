#Author: Mathumathi
@functional
Feature: Verfiy Get ALL users scenarios

  Scenario Outline: Verify user list with GET ALL requests
    Given Base URL is set
    When I send a "<method>" request to the "<endpoint>" with "<auth>"
    Then Validate "<status_code>" , "<status_line>" and other data validations

    Examples: 
      | method | endpoint | auth  | status_code | status_line        |
      | GET    | /users   | Basic |         200 | HTTP/1.1 200      |
      | GET    | /invalid | Basic |         404 | HTTP/1.1 404          |
      | GET    |          | Basic |         404 | HTTP/1.1 404        |
      | GET    | null     | Basic |         404 | HTTP/1.1 404         |
      | GET    | /users   | None  |         200 | HTTP/1.1 200                |
      | POST   | /users   | Basic |         405 | HTTP/1.1 405 |
      | PUT    | /users   | Basic |         405 | HTTP/1.1 405 |
      | DELETE | /users   | Basic |         405 | HTTP/1.1 405 |
