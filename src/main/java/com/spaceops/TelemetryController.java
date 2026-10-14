package com.spaceops;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TelemetryController {

    public record Telemetry(String spacecraft, int battery, double temperature, int fuel, String status) {
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "message", "SpaceOps is healthy");
    }

    @GetMapping("/api/telemetry")
    public Telemetry telemetry() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        int battery = r.nextInt(5, 101);
        double temperature = Math.round(r.nextDouble(-20, 100) * 10) / 10.0;
        int fuel = r.nextInt(5, 101);
        return new Telemetry("Explorer-01", battery, temperature, fuel, statusOf(battery, temperature, fuel));
    }

    static String statusOf(int battery, double temperature, int fuel) {
        boolean warning = battery < 20 || fuel < 20 || temperature > 80 || temperature < -10;
        return warning ? "WARNING" : "NORMAL";
    }
}
