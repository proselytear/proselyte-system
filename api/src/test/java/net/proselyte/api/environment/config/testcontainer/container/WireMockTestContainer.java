package net.proselyte.api.environment.config.testcontainer.container;

import org.wiremock.integrations.testcontainers.WireMockContainer;
import org.testcontainers.utility.DockerImageName;

public class WireMockTestContainer {

    public static final WireMockContainer wireMockContainer;

    static {
        wireMockContainer = new WireMockContainer(DockerImageName.parse("wiremock/wiremock:3.13.0"))
                .withExposedPorts(8080)
                .withMappingFromResource("person-service", "mappings/stubs.json");
    }
}
