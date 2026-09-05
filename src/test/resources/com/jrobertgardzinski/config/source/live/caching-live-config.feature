Feature: Caching decorator over the live source

  A TTL decorator over the live port — the same contract with bounded staleness, which is what
  keeps the level honestly LIVE: within the TTL the cached answer is served, absence included;
  once the TTL elapses the next read asks the database again. A zero TTL switches caching off.
  Which call sites read through the decorator and which through the bare adapter is a wiring
  decision, never part of the port's contract.

  Scenario: within the TTL a change in the database is not seen yet
    Given a cached database entry "min.length" with value "10" and a TTL of 30 seconds
    And the decorator has already answered once
    When the database entry "min.length" changes to "12"
    And 10 seconds pass
    Then the decorator answers "10"

  Scenario: once the TTL elapses the change is seen
    Given a cached database entry "min.length" with value "10" and a TTL of 30 seconds
    And the decorator has already answered once
    When the database entry "min.length" changes to "12"
    And 31 seconds pass
    Then the decorator answers "12"

  Scenario: absence is cached like a value
    Given a cached database miss for "min.length" and a TTL of 30 seconds
    And the decorator has already answered once
    When the database entry "min.length" changes to "10"
    And 10 seconds pass
    Then the decorator answers nothing

  Scenario: a zero TTL switches caching off
    Given a cached database entry "min.length" with value "10" and a TTL of 0 seconds
    And the decorator has already answered once
    When the database entry "min.length" changes to "12"
    Then the decorator answers "12"
