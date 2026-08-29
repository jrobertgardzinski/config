Feature: Layered configuration ladder

  One logical key, one canonical precedence law: the source bound latest in the
  lifecycle wins — live (a database row) over restart (a property or environment
  variable) over the rebuild-bound hardcoded default. The order of the rungs lives
  in the ladder and is not expressible in the API: a key declares only its name,
  its mandatory default and its mutability level, and each level is named after
  its own topmost rung — what changing the value costs. Every candidate passes
  the same validation gate; a rung holding an illegal value is skipped and the
  ladder falls through. The ladder remembers the climb: which rung answered and
  which rungs it refused on the way, so the refusal never has to stay a secret
  of the log.

  Background:
    Given the validation gate accepts only values of at least 5

  Rule: the highest legal rung the key's level can reach answers

    Scenario Outline: a live key climbs all three rungs
      Given a live key "min.length" with default 8
      And the property "min.length" is <property>
      And the database row "min.length" <row>
      Then the ladder answers <answer>

      Examples: every rung legal — the latest bound wins
        | property  | row       | answer |
        | set to 12 | holds 10  | 10     |
        | set to 12 | is absent | 12     |
        | unset     | is absent | 8      |

      Examples: an illegal rung is skipped, not repaired
        | property  | row       | answer |
        | set to 12 | holds 3   | 12     |
        | set to 4  | is absent | 8      |
        | set to 4  | holds 3   | 8      |

    Scenario: a restart key stops at the property
      Given a restart key "cache.ttl" with default 10
      And the property "cache.ttl" is set to 30
      And the database row "cache.ttl" holds 60
      Then the ladder answers 30

  Rule: the ladder reports which rung answered and what it refused on the way

    Scenario: a clean climb names its rung and refuses nothing
      Given a live key "min.length" with default 8
      And the database row "min.length" holds 10
      Then the answer comes from the "live (database)" rung
      And no rung was refused

    Scenario: every refused rung is reported with its value and the gate's reason
      Given a live key "min.length" with default 8
      And the property "min.length" is set to 4
      And the database row "min.length" holds 3
      Then the answer comes from the "rebuild (default)" rung
      And the "live (database)" rung was refused holding 3 because "value must be at least 5"
      And the "restart (properties/env)" rung was refused holding 4 because "value must be at least 5"

  Rule: the default is the terminal, so it must itself be legal

    Scenario: an illegal default refuses to build the ladder
      When a live key "min.length" is declared with default 3
      Then the declaration is rejected
