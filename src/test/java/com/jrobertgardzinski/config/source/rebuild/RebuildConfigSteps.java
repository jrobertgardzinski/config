package com.jrobertgardzinski.config.source.rebuild;

import com.jrobertgardzinski.config.domain.RebuildConfigKey;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RebuildConfigSteps {

    private RebuildConfigKey<?> key;
    private Object resolved;

    @Given("a rebuild key {string} with value {string}")
    public void aRebuildKeyWithValue(String name, String value) {
        key = new RebuildConfigKey<>(name, value);
    }

    @Given("a rebuild list key {string} with values {string}")
    public void aRebuildListKeyWithValues(String name, String csv) {
        List<String> values = Arrays.stream(csv.split(",")).map(String::trim).toList();
        key = new RebuildConfigKey<>(name, values);
    }

    @When("the rebuild source resolves the key")
    public void theRebuildSourceResolvesTheKey() {
        resolved = new RebuildConfigSource().resolve((RebuildConfigKey<String>) key);
    }

    @When("the rebuild source resolves the list key")
    public void theRebuildSourceResolvesTheListKey() {
        resolved = new RebuildConfigSource().resolve((RebuildConfigKey<List<String>>) key);
    }

    @Then("the resolved value is {string}")
    public void theResolvedValueIs(String expected) {
        assertThat(resolved).isEqualTo(expected);
    }

    @Then("the resolved list contains {string} and {string}")
    @SuppressWarnings("unchecked")
    public void theResolvedListContains(String first, String second) {
        assertThat((List<String>) resolved).containsExactlyInAnyOrder(first, second);
    }
}
