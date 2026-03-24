package com.jrobertgardzinski.config.source.repository;

import com.jrobertgardzinski.config.domain.RepositoryKey;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class RepositoryConfigSteps {

    private String scalarValue;
    private List<String> listValue;

    private RepositoryKey<?> key;
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

    @Given("a repository key {string}")
    public void aRepositoryKey(String name) {
        key = new RepositoryKey<>(name);
    }

    @Given("a list repository key {string}")
    public void aListRepositoryKey(String name) {
        key = new RepositoryKey<>(name);
    }

    @When("the repository source resolves the key")
    public void theRepositorySourceResolvesTheKey() {
        RepositoryConfigSource<String> source = new RepositoryConfigSource<>(n -> scalarValue);
        resolved = source.resolve((RepositoryKey<String>) key);
    }

    @When("the repository source resolves the list key")
    public void theRepositorySourceResolvesTheListKey() {
        RepositoryConfigSource<List<String>> source = new RepositoryConfigSource<>(n -> listValue);
        resolved = source.resolve((RepositoryKey<List<String>>) key);
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
