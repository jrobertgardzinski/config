Feature: Hardcoded configuration source

  A HardcodedKey carries its value directly in the key definition.
  No external dependencies are needed to resolve it — the value is always available at compile time.

  Scenario: Resolve a hardcoded scalar value
    Given a hardcoded key "admin-nickname" with value "admin"
    When the hardcoded source resolves the key
    Then the resolved value is "admin"

  Scenario: Resolve a hardcoded list value
    Given a hardcoded list key "blocked-domains" with values "spam.com,junk.org"
    When the hardcoded source resolves the list key
    Then the resolved list contains "spam.com" and "junk.org"
