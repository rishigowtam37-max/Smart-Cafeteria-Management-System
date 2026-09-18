package com.flowbite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Entry point for the FlowBite web application.
 *
 * <p>This is the second front end over the same domain. The original console
 * app in {@code com.flowbite.console.ConsoleApp} still runs against the exact
 * same service classes; neither knows about the other.
 *
 * <p>Every business rule - stock limits, cart totals, order IDs, sign-in -
 * lives in {@code com.flowbite.service}. The React app at the repository root
 * only renders what this application tells it.
 */
@SpringBootApplication
public class FlowBiteApplication {

    /**
     * Where the H2 order database lives, resolved before the context starts.
     *
     * <p>A JDBC file URL is relative to the working directory, and this
     * application is launched from two of them: {@code ./mvnw spring-boot:run}
     * runs in {@code backend/}, the packaged jar is run from the repository
     * root. Left alone they would open two different database files and orders
     * would appear to vanish when you switched between them. Pinning the
     * directory here means both open {@code backend/data}.
     *
     * <p>Presence of a {@code backend} directory is what distinguishes the two;
     * {@code application.properties} uses the same trick to find {@code .env}.
     * Set {@code -Dflowbite.data-dir=...} to override.
     */
    static void pinDataDirectory() {

        if (System.getProperty("flowbite.data-dir") != null) {
            return;
        }

        boolean atRepositoryRoot = Files.isDirectory(Path.of("backend"));

        System.setProperty("flowbite.data-dir",
                atRepositoryRoot ? "./backend/data" : "./data");
    }

    public static void main(String[] args) {
        pinDataDirectory();
        SpringApplication.run(FlowBiteApplication.class, args);
    }
}
