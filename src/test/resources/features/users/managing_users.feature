Feature: Managing Users
  As a consumer of the DummyJSON API
  I want to be able to manage users
  So that I can perform CRUD operations

  Scenario: Listing users
    When a user requests the list of users
    Then the response should contain a list of users
    And the total number of users should be greater than 0

  Scenario: Getting a single user by ID
    When a user requests the user with ID 1
    Then the response should contain user details
    And the user's first name should be "Emily"
    And the user's last name should be "Johnson"

  Scenario: Searching for users
    When a user searches for users with the query "John"
    Then the response should contain a list of users
    And the results should contain a user with last name "Johnson"

  Scenario: Adding a new user
    When a user creates a new user with first name "Sarah" and last name "Smith"
    Then the new user should be created successfully
    And the response should contain first name "Sarah"
    And the response should contain last name "Smith"
