Feature: Restart configuration source

  A RestartConfigKey resolves its value via RestartConfigPort, which abstracts application
  properties, environment variables, or any other source bound when the process starts —
  changing such a value costs a restart. The key name maps directly to the property name.
  A property may be absent — absence is a vacant rung for a layered resolver to fall through,
  not an error.

  Scenario: Resolve a scalar value bound at start
    Given a property "admin-nickname" with value "admin"
    And a restart key "admin-nickname"
    When the restart source resolves the key
    Then the resolved value is "admin"

  Scenario: Resolve a list value bound at start
    Given a list property "blocked-domains" with values "spam.com,junk.org"
    And a restart list key "blocked-domains"
    When the restart source resolves the list key
    Then the resolved list contains "spam.com" and "junk.org"

  Scenario: Resolve a property that is not set
    Given no property "admin-nickname"
    And a restart key "admin-nickname"
    When the restart source resolves the key
    Then the resolved value is absent
