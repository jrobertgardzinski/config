package com.jrobertgardzinski.config.source.properties;

import com.jrobertgardzinski.config.domain.PropertiesKey;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PropertiesConfigSteps {

    private String scalarValue;
    private List<String> listValue;

    private PropertiesKey<?> key;
    private Object resolved;

    @Given("a property {string} with value {string}")
    public void aPropertyWithValue(String name, String value) {
        scalarValue = value;
    }

    @Given("a list property {string} with values {string}")
    public void aListPropertyWithValues(String name, String csv) {
        listValue = Arrays.stream(csv.split(",")).map(String::trim).toList();
    }

    @Given("a properties key {string}")
    public void aPropertiesKey(String name) {
        key = new PropertiesKey<>(name);
    }

    @Given("a list properties key {string}")
    public void aListPropertiesKey(String name) {
        key = new PropertiesKey<>(name);
    }

    @When("the properties source resolves the key")
    public void thePropertiesSourceResolvesTheKey() {
        PropertiesConfigSource<String> source = new PropertiesConfigSource<>(n -> scalarValue);
        resolved = source.resolve((PropertiesKey<String>) key);
    }

    @When("the properties source resolves the list key")
    public void thePropertiesSourceResolvesTheListKey() {
        PropertiesConfigSource<List<String>> source = new PropertiesConfigSource<>(n -> listValue);
        resolved = source.resolve((PropertiesKey<List<String>>) key);
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
