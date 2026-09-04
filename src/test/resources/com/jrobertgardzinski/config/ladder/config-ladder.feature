Feature: Layered configuration ladder

  One logical key, one canonical precedence law: the source bound latest in the
  lifecycle wins — live (a database row) over restart (a property or environment
  variable) over the rebuild-bound hardcoded default. The order of the rungs lives
  in the ladder and is not expressible in the API: a key declares only its name,
  its mandatory default and its mutability level, and each level is named after
  its own topmost rung — what changing the value costs. Every candidate passes
  the same validation gate, but when depends on what the rung is bound to: the
  default and the property are fixed before the process serves, so an illegal
  one refuses to build the ladder and the deployment fails at startup; only the
  live rung — a database row, written while the system runs — is skipped and the
  ladder falls through. The ladder remembers the climb: which rung answered and
  whether it refused the row on the way, so the refusal never has to stay a
  secret of the log.

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

      Examples: an illegal row is skipped, not repaired
        | property  | row       | answer |
        | set to 12 | holds 3   | 12     |
        | unset     | holds 3   | 8      |

    Scenario: a restart key stops at the property
      Given a restart key "cache.ttl" with default 10
      And the property "cache.ttl" is set to 30
      And the database row "cache.ttl" holds 60
      Then the ladder answers 30

  Rule: the ladder reports which rung answered and whether it refused the row on the way

    Scenario: a clean climb names its rung and refuses nothing
      Given a live key "min.length" with default 8
      And the database row "min.length" holds 10
      Then the answer comes from the "live (database)" rung
      And no rung was refused

    Scenario: a refused row is reported with its value and the gate's reason
      Given a live key "min.length" with default 8
      And the property "min.length" is set to 12
      And the database row "min.length" holds 3
      Then the answer comes from the "restart (properties/env)" rung
      And the "live (database)" rung was refused holding 3 because "value must be at least 5"

  Rule: what is bound before the process serves must be legal, or there is no ladder

    Scenario: an illegal default refuses to build the ladder
      When a live key "min.length" is declared with default 3
      Then the declaration is rejected

    Scenario: an illegal property refuses to build a live key's ladder
      Given the property "min.length" is set to 4
      When a live key "min.length" is declared with default 8
      Then the declaration is rejected

    Scenario: an illegal property refuses to build a restart key's ladder
      Given the property "cache.ttl" is set to 4
      When a restart key "cache.ttl" is declared with default 10
      Then the declaration is rejected

    Scenario: an illegal property is never reached by the row's fall-through
      Given the property "min.length" is set to 4
      And the database row "min.length" holds 10
      When a live key "min.length" is declared with default 8
      Then the declaration is rejected
