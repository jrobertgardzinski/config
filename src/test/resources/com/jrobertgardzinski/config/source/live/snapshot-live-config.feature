Feature: The live level as one snapshot of the settings table

  The live level is a copy of the whole settings table, taken at most once per TTL: every key is
  answered from the same read, so a policy of five keys costs one round trip and every key is
  live in the same sense — a change, a new row or a deleted one is noticed within one TTL. A
  writer refreshes the snapshot after its own write and sees its decision at once. When the
  table cannot be read, the last snapshot stays in force for another TTL rather than the level
  quietly falling to the property. A zero TTL reads the table on every question. The TTL itself
  is bound one level below what it governs — a property, never a row — so a bad TTL can never
  delay its own correction.

  Scenario: within the TTL a change in the table is not seen yet
    Given a settings table where "min.length" is "10" and a snapshot TTL of 30 seconds
    And the snapshot has already answered once
    When the table's "min.length" changes to "12"
    And 10 seconds pass
    Then the snapshot answers "10" for "min.length"

  Scenario: once the TTL elapses the change is seen
    Given a settings table where "min.length" is "10" and a snapshot TTL of 30 seconds
    And the snapshot has already answered once
    When the table's "min.length" changes to "12"
    And 31 seconds pass
    Then the snapshot answers "12" for "min.length"

  Scenario: absence is part of the snapshot like a value
    Given an empty settings table and a snapshot TTL of 30 seconds
    And the snapshot has already answered once
    When the table's "min.length" changes to "10"
    And 10 seconds pass
    Then the snapshot answers nothing for "min.length"

  Scenario: one read of the table answers every key
    Given a settings table where "min.length" is "10" and "special.chars" is "#!" and a snapshot TTL of 30 seconds
    When the snapshot is asked for "min.length" and "special.chars" and "requires.digit"
    Then the table was read once
    And the snapshot answers "10" for "min.length"
    And the snapshot answers "#!" for "special.chars"
    And the snapshot answers nothing for "requires.digit"

  Scenario: a writer sees its own decision at once
    Given a settings table where "min.length" is "10" and a snapshot TTL of 30 seconds
    And the snapshot has already answered once
    When the table's "min.length" changes to "12"
    And the writer refreshes the snapshot
    Then the snapshot answers "12" for "min.length"

  Scenario: a zero TTL reads the table on every question
    Given a settings table where "min.length" is "10" and a snapshot TTL of 0 seconds
    And the snapshot has already answered once
    When the table's "min.length" changes to "12"
    Then the snapshot answers "12" for "min.length"

  Scenario: an unreadable table keeps the last snapshot in force
    Given a settings table where "min.length" is "10" and a snapshot TTL of 30 seconds
    And the snapshot has already answered once
    When the table becomes unreadable
    And 31 seconds pass
    Then the snapshot answers "10" for "min.length"

  Scenario: an unreadable table before the first snapshot is a vacant level, retried after a TTL
    Given a settings table where "min.length" is "10" and a snapshot TTL of 30 seconds
    And the table becomes unreadable
    Then the snapshot answers nothing for "min.length"
    When the table becomes readable again
    And 31 seconds pass
    Then the snapshot answers "10" for "min.length"
