package com.jrobertgardzinski.config.source.hardcoded;

import com.jrobertgardzinski.config.domain.HardcodedKey;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class HardcodedConfigSteps {

    private final HardcodedConfigSource source = new HardcodedConfigSource();

    private HardcodedKey<?> key;
    private Object resolved;

    @Given("a hardcoded key {string} with value {string}")
    public void aHardcodedKeyWithValue(String name, String value) {
        key = new HardcodedKey<>(name, value);
    }

    @Given("a hardcoded list key {string} with values {string}")
    public void aHardcodedListKeyWithValues(String name, String csv) {
        List<String> values = Arrays.stream(csv.split(",")).map(String::trim).toList();
        key = new HardcodedKey<>(name, values);
    }

    @When("the hardcoded source resolves the key")
    public void theHardcodedSourceResolvesTheScalarKey() {
        resolved = source.resolve((HardcodedKey<String>) key);
    }

    @When("the hardcoded source resolves the list key")
    @SuppressWarnings("unchecked")
    public void theHardcodedSourceResolvesTheListKey() {
        resolved = source.resolve((HardcodedKey<List<String>>) key);
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
