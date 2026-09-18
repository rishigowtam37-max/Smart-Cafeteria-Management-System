package com.flowbite.web;

import com.flowbite.service.CravingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * What this server supports, so the browser can adapt.
 *
 * <p>Reachable without signing in - the login screen is rendered before anyone
 * has a session, and the app needs to know what it can offer either way.
 *
 * <p>It reports only whether a feature is switched on, never why or with what.
 * The API key that decides {@code smartCravingEnabled} stays on the server.
 */
@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final CravingService cravingService;

    public ConfigController(CravingService cravingService) {
        this.cravingService = cravingService;
    }

    /** Feature switches for the browser. */
    public record ConfigDto(boolean smartCravingEnabled) {
    }

    @GetMapping
    public ConfigDto config() {
        return new ConfigDto(cravingService.isEnabled());
    }
}
