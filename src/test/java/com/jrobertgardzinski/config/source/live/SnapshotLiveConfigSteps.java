package com.jrobertgardzinski.config.source.live;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class SnapshotLiveConfigSteps {

    private final Map<String, String> table = new HashMap<>();
    private int reads;
    private boolean unreadable;

    private Instant now = Instant.parse("2026-01-01T10:00:00Z");
    private final Clock steerableClock = new Clock() {
        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    };

    private SnapshotLiveConfigPort snapshot;

    private Map<String, String> readTable() {
        reads++;
        if (unreadable)
            throw new IllegalStateException("connection refused");
        return new HashMap<>(table);
    }

    @Given("a settings table where {string} is {string} and a snapshot TTL of {int} seconds")
    public void aSettingsTableWhereIs(String name, String value, int ttlSeconds) {
        table.put(name, value);
        snapshot = new SnapshotLiveConfigPort(this::readTable, Duration.ofSeconds(ttlSeconds), steerableClock);
    }

    @Given("a settings table where {string} is {string} and {string} is {string} and a snapshot TTL of {int} seconds")
    public void aSettingsTableWhereIsAndIs(String name, String value, String other, String otherValue, int ttlSeconds) {
        table.put(name, value);
        table.put(other, otherValue);
        snapshot = new SnapshotLiveConfigPort(this::readTable, Duration.ofSeconds(ttlSeconds), steerableClock);
    }

    @Given("an empty settings table and a snapshot TTL of {int} seconds")
    public void anEmptySettingsTable(int ttlSeconds) {
        snapshot = new SnapshotLiveConfigPort(this::readTable, Duration.ofSeconds(ttlSeconds), steerableClock);
    }

    @Given("the snapshot has already answered once")
    public void theSnapshotHasAlreadyAnsweredOnce() {
        snapshot.find("min.length");
    }

    @When("the table's {string} changes to {string}")
    public void theTablesChangesTo(String name, String value) {
        table.put(name, value);
    }

    @When("{int} seconds pass")
    public void secondsPass(int seconds) {
        now = now.plusSeconds(seconds);
    }

    @When("the snapshot is asked for {string} and {string} and {string}")
    public void theSnapshotIsAskedFor(String first, String second, String third) {
        snapshot.find(first);
        snapshot.find(second);
        snapshot.find(third);
    }

    @When("the writer refreshes the snapshot")
    public void theWriterRefreshesTheSnapshot() {
        snapshot.refresh();
    }

    @When("the table becomes unreadable")
    public void theTableBecomesUnreadable() {
        unreadable = true;
    }

    @When("the table becomes readable again")
    public void theTableBecomesReadableAgain() {
        unreadable = false;
    }

    @Then("the table was read once")
    public void theTableWasReadOnce() {
        assertThat(reads).isEqualTo(1);
    }

    @Then("the snapshot answers {string} for {string}")
    public void theSnapshotAnswersFor(String expected, String name) {
        assertThat(snapshot.find(name)).isEqualTo(expected);
    }

    @Then("the snapshot answers nothing for {string}")
    public void theSnapshotAnswersNothingFor(String name) {
        assertThat(snapshot.find(name)).isNull();
    }
}
