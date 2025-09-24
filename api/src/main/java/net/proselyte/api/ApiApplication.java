package net.proselyte.api;

import net.proselyte.keycloak.api.AuthApiClient;
import net.proselyte.person.api.PersonApiClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackageClasses = {AuthApiClient.class, PersonApiClient.class})
@ConfigurationPropertiesScan
@SpringBootApplication
public class ApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }

}
