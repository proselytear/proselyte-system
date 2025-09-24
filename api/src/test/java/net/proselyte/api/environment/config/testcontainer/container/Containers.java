package net.proselyte.api.environment.config.testcontainer.container;

import lombok.experimental.UtilityClass;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.wiremock.integrations.testcontainers.WireMockContainer;

@UtilityClass
public class Containers {

    public PostgreSQLContainer postgres = PostgresTestContainer.postgresTestContainer;
    public GenericContainer keycloak = KeycloakTestContainer.keycloakTestContainer;
    public WireMockContainer wireMockContainer = WireMockTestContainer.wireMockContainer;


    public void run() {
        postgres.start();
        keycloak.start();
        wireMockContainer.start();
    }
}
