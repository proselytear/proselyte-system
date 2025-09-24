package net.proselyte.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("application.keycloak")
public record KeycloakProperties(
        String serverUrl,
        String realmUrl,
        String tokenUrl,
        String clientSecret,
        String clientId,
        String realm,
        String adminUsername,
        String adminPassword,
        String adminClientId
) {
}
