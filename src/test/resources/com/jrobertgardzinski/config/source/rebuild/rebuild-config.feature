Feature: Rebuild configuration source

  A RebuildConfigKey carries its value directly in the key definition — the value lives in the
  source code, so changing it costs a rebuild of the artifact. No external dependencies are
  needed to resolve it, which is why it is the mandatory terminal of every ladder.

  Scenario: Resolve a rebuild-bound scalar value
    Given a rebuild key "admin-nickname" with value "admin"
    When the rebuild source resolves the key
    Then the resolved value is "admin"

  Scenario: Resolve a rebuild-bound list value
    Given a rebuild list key "blocked-domains" with values "spam.com,junk.org"
    When the rebuild source resolves the list key
    Then the resolved list contains "spam.com" and "junk.org"
