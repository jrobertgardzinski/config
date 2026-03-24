Feature: Properties configuration source

  A PropertiesKey resolves its value via PropertiesConfigPort, which abstracts
  application.properties, environment variables, or any other property source.
  The key name maps directly to the property name.

  Scenario: Resolve a scalar value from properties
    Given a property "admin-nickname" with value "admin"
    And a properties key "admin-nickname"
    When the properties source resolves the key
    Then the resolved value is "admin"

  Scenario: Resolve a list value from properties
    Given a list property "blocked-domains" with values "spam.com,junk.org"
    And a list properties key "blocked-domains"
    When the properties source resolves the list key
    Then the resolved list contains "spam.com" and "junk.org"
