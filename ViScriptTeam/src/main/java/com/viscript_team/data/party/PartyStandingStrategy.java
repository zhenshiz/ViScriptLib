package com.viscript_team.data.party;

import lombok.Getter;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Getter
public enum PartyStandingStrategy {
    MIN("min"),
    AVERAGE("average"),
    LEADER("leader"),
    MAX("max");

    private final String id;

    PartyStandingStrategy(String id) {
        this.id = id;
    }

    public static PartyStandingStrategy defaultStrategy() {
        return MIN;
    }

    public static PartyStandingStrategy orDefault(@Nullable PartyStandingStrategy strategy) {
        return strategy == null ? defaultStrategy() : strategy;
    }

    public static Optional<PartyStandingStrategy> byName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        String normalized = name.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(strategy -> strategy.id.equals(normalized) || strategy.name().toLowerCase(Locale.ROOT).equals(normalized))
                .findFirst();
    }

    public static PartyStandingStrategy byNameOrDefault(String name) {
        return byName(name).orElse(defaultStrategy());
    }

    public static List<String> ids() {
        return Arrays.stream(values()).map(PartyStandingStrategy::getId).toList();
    }
}
