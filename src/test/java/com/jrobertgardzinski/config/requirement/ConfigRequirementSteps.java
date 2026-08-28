package com.jrobertgardzinski.config.requirement;

import com.jrobertgardzinski.config.source.restart.RestartConfigSource;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class ConfigRequirementSteps {

    private final Map<String, Integer> properties = new HashMap<>();

    private Consumer<Integer> gate;
    private String keyName;

    private Integer answer;
    private Throwable refusal;

    @Given("the required gate accepts only values of at least {int}")
    public void theRequiredGateAcceptsOnlyValuesOfAtLeast(int floor) {
        gate = value -> {
            if (value < floor)
                throw new IllegalArgumentException("value must be at least " + floor);
        };
    }

    @Given("a required restart key {string}")
    public void aRequiredRestartKey(String name) {
        keyName = name;
    }

    @Given("the required property {string} is set to {int}")
    public void theRequiredPropertyIsSetTo(String name, int value) {
        properties.put(name, value);
    }

    @When("the requirement resolves")
    public void theRequirementResolves() {
        answer = requirement().resolve();
    }

    @When("the requirement resolves expecting refusal")
    public void theRequirementResolvesExpectingRefusal() {
        refusal = catchThrowable(() -> requirement().resolve());
    }

    @Then("the requirement answers {int}")
    public void theRequirementAnswers(int expected) {
        assertThat(answer).isEqualTo(expected);
    }

    @Then("the start is refused because the key is not set")
    public void theStartIsRefusedBecauseTheKeyIsNotSet() {
        assertThat(refusal).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("is not set");
    }

    @Then("the start is refused because the value is illegal")
    public void theStartIsRefusedBecauseTheValueIsIllegal() {
        assertThat(refusal).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("illegal value");
    }

    private ConfigRequirement<Integer> requirement() {
        return ConfigRequirement.restart(keyName, gate, new RestartConfigSource<>(properties::get));
    }
}
