package io.github.lijinhong11.supermines.utils;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class NumberUtils {
    private NumberUtils() {}

    public static boolean matchChance(double chancePercent) {
        return (chancePercent / 100) >= 1 || ThreadLocalRandom.current().nextDouble(1) < (chancePercent / 100);
    }

    public static <T> T weightedRandom(Map<T, Double> map) {
        double totalWeight =
                map.values().stream().mapToDouble(Double::doubleValue).sum();
        double random = Math.random() * totalWeight;
        double current = 0.0;
        for (Map.Entry<T, Double> entry : map.entrySet()) {
            current += entry.getValue();
            if (random <= current) {
                return entry.getKey();
            }
        }

        return map.keySet().iterator().next();
    }
}
