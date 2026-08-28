Feature: Live configuration source

  A LiveConfigKey resolves its value via LiveConfigPort, which abstracts a store the running
  system re-reads on its own — a database table an administrator edits. Changing such a value
  costs nothing beyond the staleness bound of whatever cache sits in front. Values may be
  absent — the port reports absence and a layered resolver falls through.

  Scenario: Resolve a scalar value from the live store
    Given a database entry "admin-nickname" with value "admin"
    And a live key "admin-nickname"
    When the live source resolves the key
    Then the resolved value is "admin"

  Scenario: Resolve a list value from the live store
    Given a database list entry "blocked-domains" with values "spam.com,junk.org"
    And a live list key "blocked-domains"
    When the live source resolves the list key
    Then the resolved list contains "spam.com" and "junk.org"

  Scenario: Resolve a scalar value that is absent from the live store
    Given no database entry for "admin-nickname"
    And a live key "admin-nickname"
    When the live source resolves the key
    Then the resolved value is absent
