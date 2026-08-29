package com.jrobertgardzinski.config.ladder;

import com.jrobertgardzinski.config.source.live.LiveConfigSource;
import com.jrobertgardzinski.config.source.restart.RestartConfigSource;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class ConfigLadderSteps {

    private final Map<String, Integer> databaseRows = new HashMap<>();
    private final Map<String, Integer> properties = new HashMap<>();

    private Consumer<Integer> gate;
    private String keyName;
    private int defaultValue;
    private boolean liveLevel;

    private Resolution<Integer> answer;
    private Throwable declarationFailure;

    @Given("the validation gate accepts only values of at least {int}")
    public void theValidationGateAcceptsOnlyValuesOfAtLeast(int floor) {
        gate = value -> {
            if (value < floor)
                throw new IllegalArgumentException("value must be at least " + floor);
        };
    }

    @Given("a live key {string} with default {int}")
    public void aLiveKeyWithDefault(String name, int def) {
        keyName = name;
        defaultValue = def;
        liveLevel = true;
    }

    @Given("a restart key {string} with default {int}")
    public void aRestartKeyWithDefault(String name, int def) {
        keyName = name;
        defaultValue = def;
        liveLevel = false;
    }

    @Given("the property {string} is set to {int}")
    public void thePropertyIsSetTo(String name, int value) {
        properties.put(name, value);
    }

    @Given("the property {string} is unset")
    public void thePropertyIsUnset(String name) {
        properties.remove(name);
    }

    @Given("the database row {string} holds {int}")
    public void theDatabaseRowHolds(String name, int value) {
        databaseRows.put(name, value);
    }

    @Given("the database row {string} is absent")
    public void theDatabaseRowIsAbsent(String name) {
        databaseRows.remove(name);
    }

    @When("a live key {string} is declared with default {int}")
    public void aLiveKeyIsDeclaredWithDefault(String name, int def) {
        keyName = name;
        defaultValue = def;
        liveLevel = true;
        declarationFailure = catchThrowable(this::buildLadder);
    }

    /** Resolution is a side-effect-free query, so the scenarios skip the When and the Then asks. */
    private Resolution<Integer> answer() {
        if (answer == null)
            answer = buildLadder().resolution();
        return answer;
    }

    @Then("the ladder answers {int}")
    public void theLadderAnswers(int expected) {
        assertThat(answer().value()).isEqualTo(expected);
    }

    @Then("the answer comes from the {string} rung")
    public void theAnswerComesFromTheRung(String source) {
        assertThat(answer().source()).isEqualTo(source);
    }

    @Then("no rung was refused")
    public void noRungWasRefused() {
        assertThat(answer().rejected()).isEmpty();
    }

    @Then("the {string} rung was refused holding {int} because {string}")
    public void theRungWasRefused(String source, int held, String reason) {
        assertThat(answer().rejected()).contains(new Resolution.Rejected<>(source, held, reason));
    }

    @Then("the declaration is rejected")
    public void theDeclarationIsRejected() {
        assertThat(declarationFailure).isInstanceOf(IllegalArgumentException.class);
    }

    private ConfigLadder<Integer> buildLadder() {
        RestartConfigSource<Integer> restartSource = new RestartConfigSource<>(properties::get);
        return liveLevel
                ? ConfigLadder.live(keyName, defaultValue, gate,
                        new LiveConfigSource<>(databaseRows::get), restartSource)
                : ConfigLadder.restart(keyName, defaultValue, gate, restartSource);
    }
}
