Feature: Browsing Products
  As a consumer of the DummyJSON API
  I want to be able to browse products
  So that I can find products I am interested in

  Scenario: Listing all products
    When a user requests the list of products
    Then the response should contain a list of products
    And the total number of products should be greater than 0

  Scenario: Getting a single product by ID
    When a user requests the product with ID 1
    Then the response should contain product details
    And the product should have a title
    And the product should have a price greater than 0

  Scenario: Searching for products
    When a user searches for products with the query "laptop"
    Then the response should contain a list of products
    And all returned products should be related to "laptop"

  Scenario: Browsing products by category
    When a user browses products in the "smartphones" category
    Then the response should contain a list of products
    And all returned products should be in the "smartphones" category
