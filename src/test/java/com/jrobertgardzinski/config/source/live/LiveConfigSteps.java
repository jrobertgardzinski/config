package com.jrobertgardzinski.config.source.live;

import com.jrobertgardzinski.config.domain.LiveConfigKey;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class LiveConfigSteps {

    private String scalarValue;
    private List<String> listValue;

    private LiveConfigKey<?> key;
    private Optional<?> resolved;

    @Given("a database entry {string} with value {string}")
    public void aDatabaseEntryWithValue(String name, String value) {
        scalarValue = value;
    }

    @Given("a database list entry {string} with values {string}")
    public void aDatabaseListEntryWithValues(String name, String csv) {
        listValue = Arrays.stream(csv.split(",")).map(String::trim).toList();
    }

    @Given("no database entry for {string}")
    public void noDatabaseEntryFor(String name) {
        scalarValue = null;
    }

    @Given("a live key {string}")
    public void aLiveKey(String name) {
        key = new LiveConfigKey<>(name);
    }

    @Given("a live list key {string}")
    public void aLiveListKey(String name) {
        key = new LiveConfigKey<>(name);
    }

    @When("the live source resolves the key")
    public void theLiveSourceResolvesTheKey() {
        LiveConfigSource<String> source = new LiveConfigSource<>(n -> scalarValue);
        resolved = source.resolve((LiveConfigKey<String>) key);
    }

    @When("the live source resolves the list key")
    public void theLiveSourceResolvesTheListKey() {
        LiveConfigSource<List<String>> source = new LiveConfigSource<>(n -> listValue);
        resolved = source.resolve((LiveConfigKey<List<String>>) key);
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
