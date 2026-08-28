Feature: Layered configuration ladder

  One logical key, one canonical precedence law: the source bound latest in the
  lifecycle wins — live (a database row) over restart (a property or environment
  variable) over the rebuild-bound hardcoded default. The order of the rungs lives
  in the ladder and is not expressible in the API: a key declares only its name,
  its mandatory default and its mutability level, and each level is named after
  its own topmost rung — what changing the value costs. Every candidate passes
  the same validation gate; a rung holding an illegal value is skipped and the
  ladder falls through.

  Background:
    Given the validation gate accepts only values of at least 5

  Scenario: the live value wins over the property and the default
    Given a live key "min.length" with default 8
    And the property "min.length" is set to 12
    And the database row "min.length" holds 10
    When the ladder resolves
    Then the ladder answers 10

  Scenario: without a database row the property wins over the default
    Given a live key "min.length" with default 8
    And the property "min.length" is set to 12
    When the ladder resolves
    Then the ladder answers 12

  Scenario: with no database row and no property the default answers
    Given a live key "min.length" with default 8
    When the ladder resolves
    Then the ladder answers 8

  Scenario: an illegal live value falls through to the property
    Given a live key "min.length" with default 8
    And the property "min.length" is set to 12
    And the database row "min.length" holds 3
    When the ladder resolves
    Then the ladder answers 12

  Scenario: illegal live and property values fall through to the default
    Given a live key "min.length" with default 8
    And the property "min.length" is set to 4
    And the database row "min.length" holds 3
    When the ladder resolves
    Then the ladder answers 8

  Scenario: a restart-level key never consults the database
    Given a restart key "cache.ttl" with default 10
    And the property "cache.ttl" is set to 30
    And the database row "cache.ttl" holds 60
    When the ladder resolves
    Then the ladder answers 30

  Scenario: an illegal default refuses to build the ladder
    When a live key "min.length" is declared with default 3
    Then the declaration is rejected
