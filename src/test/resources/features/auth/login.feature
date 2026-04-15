Feature: User Authentication
  As a user of the DummyJSON API
  I want to be able to log in with my credentials
  So that I can access authenticated endpoints

  Scenario: Logging in with valid credentials
    When a user logs in with username "emilys" and password "emilyspass"
    Then the login should be successful
    And the response should include an access token
    And the response should include user details for "emilys"

  Scenario: Logging in with invalid credentials
    When a user logs in with username "emilys" and password "wrongpassword"
    Then the login should fail with status code 400

  Scenario: Accessing authenticated user profile
    Given a user is logged in with username "emilys" and password "emilyspass"
    When the user requests their profile
    Then the profile should contain the username "emilys"
