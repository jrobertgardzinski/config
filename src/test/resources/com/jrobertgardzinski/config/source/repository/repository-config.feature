Feature: Repository configuration source

  A RepositoryKey resolves its value via RepositoryConfigPort, which abstracts
  database access. The key name is used as the identifier for the lookup.
  Values may be absent — the port returns Optional for scalar keys.

  Scenario: Resolve a scalar value from the repository
    Given a database entry "admin-nickname" with value "admin"
    And a repository key "admin-nickname"
    When the repository source resolves the key
    Then the resolved value is "admin"

  Scenario: Resolve a list value from the repository
    Given a database list entry "blocked-domains" with values "spam.com,junk.org"
    And a list repository key "blocked-domains"
    When the repository source resolves the list key
    Then the resolved list contains "spam.com" and "junk.org"

  Scenario: Resolve a scalar value that is absent from the repository
    Given no database entry for "admin-nickname"
    And a repository key "admin-nickname"
    When the repository source resolves the key
    Then the resolved value is absent
