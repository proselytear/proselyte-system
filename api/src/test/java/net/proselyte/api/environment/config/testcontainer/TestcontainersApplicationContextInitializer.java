package net.proselyte.api.environment.config.testcontainer;

import net.proselyte.api.environment.config.testcontainer.container.WireMockTestContainer;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

import static net.proselyte.api.environment.config.testcontainer.container.KeycloakTestContainer.keycloakTestContainer;

public class TestcontainersApplicationContextInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final String REALM = "individual"; // ваш realm из realm-config.json
    private static final String CLIENT_FOR_PASSWORD = "public-client"; // имя клиента, у которого включен DAG (замените!)

    @Override
    public void initialize(ConfigurableApplicationContext ctx) {
        net.proselyte.api.environment.config.testcontainer.container.Containers.run();

        String kcBase = "http://" + keycloakTestContainer.getHost() + ":" + keycloakTestContainer.getMappedPort(8080);
        String wireMockBase = WireMockTestContainer.wireMockContainer.getBaseUrl();


        Map<String, Object> p = new HashMap<>();
        // ваши собственные проперти
        p.put("application.person.url", wireMockBase);
        p.put("application.keycloak.serverUrl", kcBase);
        p.put("application.keycloak.realm", REALM);

        // для Resource Server (валидатор JWT)
        p.put("spring.security.oauth2.resourceserver.jwt.issuer-uri", kcBase + "/realms/" + REALM);

        // если TokenService/KeycloakClient читает из spring.oauth client/provider:
        p.put("spring.security.oauth2.client.provider.keycloak.token-uri",
                kcBase + "/realms/" + REALM + "/protocol/openid-connect/token");
        p.put("spring.security.oauth2.client.provider.keycloak.authorization-uri",
                kcBase + "/realms/" + REALM + "/protocol/openid-connect/auth");
        p.put("spring.security.oauth2.client.provider.keycloak.user-info-uri",
                kcBase + "/realms/" + REALM + "/protocol/openid-connect/userinfo");
        p.put("spring.security.oauth2.client.provider.keycloak.jwk-set-uri",
                kcBase + "/realms/" + REALM + "/protocol/openid-connect/certs");

        // если для password login используете регистрацию spring client (по желанию)
        p.put("spring.security.oauth2.client.registration." + CLIENT_FOR_PASSWORD + ".provider", "keycloak");
        p.put("spring.security.oauth2.client.registration." + CLIENT_FOR_PASSWORD + ".client-id", CLIENT_FOR_PASSWORD);
        // если клиент секретный:
        // p.put("spring.security.oauth2.client.registration." + CLIENT_FOR_PASSWORD + ".client-secret", "secret");
        p.put("spring.security.oauth2.client.registration." + CLIENT_FOR_PASSWORD + ".authorization-grant-type", "password");

        ctx.getEnvironment().getPropertySources()
                .addFirst(new MapPropertySource("override-props", p));
    }
}
