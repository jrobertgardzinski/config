Feature: Required configuration

  The ladder's opposite: a key the system refuses to start without. No default exists, because
  for this kind of key a default is a lie — a real secret in the repository, or a "working"
  value that masks a misconfigured deployment. Only the restart source (properties, environment
  variables) can carry a requirement: a rebuild-only value is a constant the compiler already
  enforces, and a boot that depends on a database row is a boot-order trap — both are
  unrepresentable in the API. An illegal value refuses the start exactly like an absent one,
  because there is nothing to fall through to.

  Background:
    Given the required gate accepts only values of at least 5

  Scenario: a value present and legal is returned
    Given a required restart key "db.pool.size"
    And the required property "db.pool.size" is set to 10
    When the requirement resolves
    Then the requirement answers 10

  Scenario: an absent value refuses the start
    Given a required restart key "db.pool.size"
    When the requirement resolves expecting refusal
    Then the start is refused because the key is not set

  Scenario: an illegal value refuses the start
    Given a required restart key "db.pool.size"
    And the required property "db.pool.size" is set to 3
    When the requirement resolves expecting refusal
    Then the start is refused because the value is illegal
