package com.jrobertgardzinski.config.source.restart;

import com.jrobertgardzinski.config.domain.RestartConfigKey;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class RestartConfigSteps {

    private String scalarValue;
    private List<String> listValue;

    private RestartConfigKey<?> key;
    private Optional<?> resolved;

    @Given("a property {string} with value {string}")
    public void aPropertyWithValue(String name, String value) {
        scalarValue = value;
    }

    @Given("a list property {string} with values {string}")
    public void aListPropertyWithValues(String name, String csv) {
        listValue = Arrays.stream(csv.split(",")).map(String::trim).toList();
    }

    @Given("no property {string}")
    public void noProperty(String name) {
        scalarValue = null;
    }

    @Given("a restart key {string}")
    public void aRestartKey(String name) {
        key = new RestartConfigKey<>(name);
    }

    @Given("a restart list key {string}")
    public void aRestartListKey(String name) {
        key = new RestartConfigKey<>(name);
    }

    @When("the restart source resolves the key")
    public void theRestartSourceResolvesTheKey() {
        RestartConfigSource<String> source = new RestartConfigSource<>(n -> scalarValue);
        resolved = source.resolve((RestartConfigKey<String>) key);
    }

    @When("the restart source resolves the list key")
    public void theRestartSourceResolvesTheListKey() {
        RestartConfigSource<List<String>> source = new RestartConfigSource<>(n -> listValue);
        resolved = source.resolve((RestartConfigKey<List<String>>) key);
    }

    @Then("the resolved value is {string}")
    public void theResolvedValueIs(String expected) {
        assertThat(resolved.orElseThrow()).isEqualTo(expected);
    }

    @Then("the resolved list contains {string} and {string}")
    @SuppressWarnings("unchecked")
    public void theResolvedListContains(String first, String second) {
        assertThat((List<String>) resolved.orElseThrow()).containsExactlyInAnyOrder(first, second);
    }

    @Then("the resolved value is absent")
    public void theResolvedValueIsAbsent() {
        assertThat(resolved).isEmpty();
    }
}
