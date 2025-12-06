package com.transactionsync.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class PrivvyCredentialsLoader {

    private static final String PRIVVY_DIR = System.getProperty("user.home") + File.separator + ".privvy";
    private static final String CREDENTIALS_FILE = PRIVVY_DIR + File.separator + "credentials";
    private static final Map<String, String> credentials = new HashMap<>();

    static {
        loadCredentials();
    }

    private static void loadCredentials() {
        Path credentialsPath = Paths.get(CREDENTIALS_FILE);
        
        if (Files.exists(credentialsPath)) {
            try {
                Files.lines(credentialsPath).forEach(line -> {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        int equalsIndex = line.indexOf('=');
                        if (equalsIndex > 0) {
                            String key = line.substring(0, equalsIndex).trim();
                            String value = line.substring(equalsIndex + 1).trim();
                            credentials.put(key, value);
                        }
                    }
                });
            } catch (IOException e) {
                System.err.println("Warning: Could not load Privvy credentials from " + CREDENTIALS_FILE);
            }
        }
    }

    public static String getApiUrl() {
        String envValue = System.getenv("PRIVVY_API_URL");
        return credentials.getOrDefault("api_url", envValue != null ? envValue : "");
    }

    public static String getEmail() {
        String envValue = System.getenv("PRIVVY_EMAIL");
        return credentials.getOrDefault("email", envValue != null ? envValue : "");
    }

    public static String getPassword() {
        String envValue = System.getenv("PRIVVY_PASSWORD");
        return credentials.getOrDefault("password", envValue != null ? envValue : "");
    }
}

