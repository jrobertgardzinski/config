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

public class CachingLiveConfigSteps {

    private final Map<String, String> databaseRows = new HashMap<>();

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

    private CachingLiveConfigPort<String> decorator;
    private String cachedKeyName;

    @Given("a cached database entry {string} with value {string} and a TTL of {int} seconds")
    public void aCachedDatabaseEntryWithValueAndATtlOfSeconds(String name, String value, int ttlSeconds) {
        databaseRows.put(name, value);
        cachedKeyName = name;
        decorator = new CachingLiveConfigPort<>(databaseRows::get,
                Duration.ofSeconds(ttlSeconds), steerableClock);
    }

    @Given("a cached database miss for {string} and a TTL of {int} seconds")
    public void aCachedDatabaseMissForAndATtlOfSeconds(String name, int ttlSeconds) {
        cachedKeyName = name;
        decorator = new CachingLiveConfigPort<>(databaseRows::get,
                Duration.ofSeconds(ttlSeconds), steerableClock);
    }

    @Given("the decorator has already answered once")
    public void theDecoratorHasAlreadyAnsweredOnce() {
        decorator.find(cachedKeyName);
    }

    @When("the database entry {string} changes to {string}")
    public void theDatabaseEntryChangesTo(String name, String value) {
        databaseRows.put(name, value);
    }

    @When("{int} seconds pass")
    public void secondsPass(int seconds) {
        now = now.plusSeconds(seconds);
    }

    @Then("the decorator answers {string}")
    public void theDecoratorAnswers(String expected) {
        assertThat(decorator.find(cachedKeyName)).isEqualTo(expected);
    }

    @Then("the decorator answers nothing")
    public void theDecoratorAnswersNothing() {
        assertThat(decorator.find(cachedKeyName)).isNull();
    }
}
