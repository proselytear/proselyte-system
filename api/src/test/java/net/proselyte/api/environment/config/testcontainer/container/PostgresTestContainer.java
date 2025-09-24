package net.proselyte.api.environment.config.testcontainer.container;

import org.testcontainers.containers.PostgreSQLContainer;

import net.proselyte.api.environment.config.testcontainer.util.Setting;

public class PostgresTestContainer {

    public static final PostgreSQLContainer postgresTestContainer;

    static {
        postgresTestContainer = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("keycloak")
            .withUsername("keycloak")
            .withPassword("keycloak")
            .withNetwork(Setting.GLOBAL_NETWORK)
            .withNetworkAliases("postgres");
    }
}
