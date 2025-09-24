package net.proselyte.api.environment.config.testcontainer.service;

import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import net.proselyte.individual.dto.IndividualWriteDto;
import net.proselyte.individual.dto.TokenResponse;
import net.proselyte.individual.dto.UserInfoResponse;
import net.proselyte.individual.dto.UserLoginRequest;

@Component
public class IndividualApiTestService {

    private final RestTemplate restTemplate;
    private final Environment env;

    public IndividualApiTestService(RestTemplate restTemplate, Environment env) {
        this.restTemplate = restTemplate;
        this.env = env;
    }

    private String baseUrl() {
        Integer port = env.getProperty("local.server.port", Integer.class);
        if (port == null || port == 0) {
            port = env.getProperty("server.port", Integer.class, 8080);
        }
        return "http://localhost:" + port + "/v1";
    }

    public TokenResponse register(IndividualWriteDto request) {
        return restTemplate.postForObject(baseUrl() + "/auth/registration", request, TokenResponse.class);
    }

    public TokenResponse login(UserLoginRequest request) {
        return restTemplate.postForObject(baseUrl() + "/auth/login", request, TokenResponse.class);
    }

    public UserInfoResponse getMe(String accessToken) {
        var headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        var resp = restTemplate.exchange(baseUrl() + "/auth/me", HttpMethod.GET, new HttpEntity<>(headers), UserInfoResponse.class);
        return resp.getBody();
    }
}
