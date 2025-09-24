package net.proselyte.api.spec.integration;

import net.proselyte.api.ApiApplication;
import net.proselyte.api.environment.config.testcontainer.container.Containers;
import net.proselyte.api.environment.config.testcontainer.container.KeycloakTestContainer;
import net.proselyte.api.environment.config.testcontainer.container.WireMockTestContainer;
import net.proselyte.api.environment.config.testcontainer.data.DtoCreator;
import net.proselyte.api.environment.config.testcontainer.service.IndividualApiTestService;
import net.proselyte.api.environment.config.testcontainer.service.KeycloakApiTestService;
import net.proselyte.api.spec.support.TestSupportConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;


@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {ApiApplication.class,
                TestSupportConfig.class})
@ActiveProfiles("test")
public abstract class LifecycleSpecification {
    protected final DtoCreator dtoCreator =
            new DtoCreator();

    @Autowired
    protected IndividualApiTestService individualControllerService;
    @Autowired
    protected KeycloakApiTestService keycloakApiTestService;

    static {
        Containers.run();
    }

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry r) {
        final String kcBase = "http://" +
                KeycloakTestContainer.keycloakTestContainer.getHost() + ":" +
                KeycloakTestContainer.keycloakTestContainer.getFirstMappedPort();

        final String wireMockBase = "http://" +
                WireMockTestContainer.wireMockContainer.getHost() + ":" +
                WireMockTestContainer.wireMockContainer.getFirstMappedPort();

        r.add("application.keycloak.serverUrl", () -> kcBase);
        r.add("application.keycloak.realm", () -> "individual");
        r.add("application.keycloak.clientId", () -> "individual");
        r.add("application.keycloak.clientSecret", () -> "FaxzBgk7pkyattBrV8MlVCVg80jjZKo5");
        r.add("application.keycloak.adminClientId", () -> "admin-cli");
        r.add("application.keycloak.adminUsername", () -> "admin");
        r.add("application.keycloak.adminPassword", () -> "admin");

        r.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> kcBase + "/realms/individual");

        r.add("auth.url", () -> kcBase);
        r.add("users.url", () -> kcBase);
        r.add("person.url", () -> wireMockBase);
    }




    @AfterEach
    public void clear() {
        keycloakApiTestService.clear();
    }
}
