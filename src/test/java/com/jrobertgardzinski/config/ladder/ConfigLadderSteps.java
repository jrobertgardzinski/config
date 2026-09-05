package com.jrobertgardzinski.config.ladder;

import com.jrobertgardzinski.config.source.live.LiveConfigPort;
import com.jrobertgardzinski.config.source.restart.RestartConfigPort;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class ConfigLadderSteps {

    private final Map<String, Integer> databaseRows = new HashMap<>();
    private final Map<String, Integer> properties = new HashMap<>();
    private final LiveConfigPort<Integer> rows = databaseRows::get;
    private final RestartConfigPort<Integer> props = properties::get;

    private Consumer<Integer> gate;
    private String keyName;
    private List<Rung<Integer>> rungs;
    private Resolution<Integer> answer;
    private Throwable declarationFailure;

    @Given("the validation gate accepts only values of at least {int}")
    public void theValidationGateAcceptsOnlyValuesOfAtLeast(int floor) {
        gate = value -> {
            if (value < floor)
                throw new IllegalArgumentException("value must be at least " + floor);
        };
    }

    @Given("a ladder for {string} with rungs live, restart and rebuild default {int}")
    public void aLadderOverAllThree(String name, int def) {
        declare(name, List.of(Rung.live(rows), Rung.restart(props), Rung.rebuild(def)));
    }

    @Given("a ladder for {string} with rungs restart and rebuild default {int}")
    public void aLadderWithoutLive(String name, int def) {
        declare(name, List.of(Rung.restart(props), Rung.rebuild(def)));
    }

    @Given("a ladder for {string} with rungs live and rebuild default {int}")
    public void aLadderWithoutRestart(String name, int def) {
        declare(name, List.of(Rung.live(rows), Rung.rebuild(def)));
    }

    @Given("a ladder for {string} with rungs rebuild default {int}")
    public void aLadderOfTheDefaultAlone(String name, int def) {
        declare(name, List.of(Rung.rebuild(def)));
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

    @When("a ladder for {string} is declared with rungs live, restart and rebuild default {int}")
    public void aLadderOverAllThreeIsDeclared(String name, int def) {
        declareNow(name, List.of(Rung.live(rows), Rung.restart(props), Rung.rebuild(def)));
    }

    @When("a ladder for {string} is declared with rungs restart and rebuild default {int}")
    public void aLadderWithoutLiveIsDeclared(String name, int def) {
        declareNow(name, List.of(Rung.restart(props), Rung.rebuild(def)));
    }

    @When("a ladder for {string} is declared with rungs live and restart only")
    public void aLadderWithoutRebuildIsDeclared(String name) {
        declareNow(name, List.of(Rung.live(rows), Rung.restart(props)));
    }

    @When("a ladder for {string} is declared with rungs restart, live and rebuild default {int}")
    public void aLadderOutOfOrderIsDeclared(String name, int def) {
        declareNow(name, List.of(Rung.restart(props), Rung.live(rows), Rung.rebuild(def)));
    }

    @When("a ladder for {string} is declared with rungs live, live and rebuild default {int}")
    public void aLadderWithADoubledLevelIsDeclared(String name, int def) {
        declareNow(name, List.of(Rung.live(rows), Rung.live(rows), Rung.rebuild(def)));
    }

    private void declare(String name, List<Rung<Integer>> rungs) {
        keyName = name;
        this.rungs = new ArrayList<>(rungs);
    }

    private void declareNow(String name, List<Rung<Integer>> rungs) {
        declare(name, rungs);
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

    @Then("the answer comes from the {string} level")
    public void theAnswerComesFromTheLevel(String source) {
        assertThat(answer().source()).isEqualTo(source);
    }

    @Then("no level was refused")
    public void noLevelWasRefused() {
        assertThat(answer().rejected()).isEmpty();
    }

    @Then("the {string} level was refused holding {int} because {string}")
    public void theLevelWasRefused(String source, int held, String reason) {
        assertThat(answer().rejected()).contains(new Resolution.Rejected<>(source, held, reason));
    }

    @Then("the declaration is rejected")
    public void theDeclarationIsRejected() {
        assertThat(declarationFailure).isInstanceOf(IllegalArgumentException.class);
    }

    @Then("the declaration is rejected naming the {string} level")
    public void theDeclarationIsRejectedNamingTheLevel(String level) {
        assertThat(declarationFailure).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(keyName)
                .hasMessageContaining(level);
    }

    @SuppressWarnings("unchecked")
    private ConfigLadder<Integer> buildLadder() {
        return ConfigLadder.of(keyName, gate, rungs.toArray(Rung[]::new));
    }
}
