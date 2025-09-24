package net.proselyte.api.client;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.proselyte.api.config.KeycloakProperties;
import net.proselyte.api.dto.KeycloakCredentialsRepresentation;
import net.proselyte.api.dto.KeycloakUserRepresentation;
import net.proselyte.api.exception.ApiException;
import net.proselyte.api.util.UserIdExtractor;
import net.proselyte.keycloak.dto.TokenRefreshRequest;
import net.proselyte.keycloak.dto.TokenResponse;
import net.proselyte.keycloak.dto.UserLoginRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeycloakClient {

    private static final String BEARER_PREFIX = "Bearer ";

    private final WebClient webClient;
    private final KeycloakProperties props;

    private String userRegistrationUrl;
    private String userPasswordResetUrl;
    private String userByIdUrl;

    @PostConstruct
    public void init() {
        this.userRegistrationUrl = props.serverUrl() + "/admin/realms/" + props.realm() + "/users";
        this.userByIdUrl = userRegistrationUrl + "/{id}";
        this.userPasswordResetUrl = userByIdUrl + "/reset-password";
    }

    @WithSpan("keycloakClient.login")
    public Mono<TokenResponse> login(UserLoginRequest req) {
        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "password");
        form.add("username", req.getEmail());
        form.add("password", req.getPassword());
        form.add("client_id", props.clientId());
        addIfNotBlank(form, "client_secret", props.clientSecret());

        return webClient.post()
                .uri(props.tokenUrl()) // http://localhost:8080/realms/individual/protocol/openid-connect/token
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(form)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::toApiException)
                .bodyToMono(TokenResponse.class);
    }

    @WithSpan("keycloakClient.adminLogin")
    public Mono<TokenResponse> adminLogin() {
        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "password");
        form.add("client_id", props.adminClientId());
        form.add("username", props.adminUsername());
        form.add("password", props.adminPassword());

        return webClient.post()
                .uri(props.tokenUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(form)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::toApiException)
                .bodyToMono(TokenResponse.class);
    }

    @WithSpan("keycloakClient.refreshToken")
    public Mono<TokenResponse> refreshToken(TokenRefreshRequest req) {
        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", req.getRefreshToken());
        form.add("client_id", props.clientId());
        addIfNotBlank(form, "client_secret", props.clientSecret());

        return webClient.post()
                .uri(props.tokenUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(form)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::toApiException)
                .bodyToMono(TokenResponse.class);
    }

    @WithSpan("keycloakClient.registerUser")
    public Mono<String> registerUser(TokenResponse adminToken, KeycloakUserRepresentation user) {
        return webClient.post()
                .uri(userRegistrationUrl)
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + adminToken.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(user)
                .exchangeToMono(this::extractIdFromPath);
    }

    private Mono<String> extractIdFromPath(ClientResponse response) {
        if (response.statusCode().equals(HttpStatus.CREATED)) {
            var location = response.headers().asHttpHeaders().getLocation();
            if (location == null) return Mono.error(new ApiException("Location header missing"));
            return Mono.just(UserIdExtractor.extractIdFromPath(location.getPath()));
        }
        return response.bodyToMono(String.class)
                .flatMap(body -> Mono.error(new ApiException("User creation failed: " + body)));
    }

    @WithSpan("keycloakClient.resetUserPassword")
    public Mono<Void> resetUserPassword(String userId, KeycloakCredentialsRepresentation dto, String adminAccessToken) {
        return webClient.put()
                .uri(userPasswordResetUrl, userId)
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + adminAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> Mono.error(new ApiException(
                                        "KC reset-password failed " + resp.statusCode() + ": " + body)))
                )
                .toBodilessEntity()
                .then();
    }


    @WithSpan("keycloakClient.resetUserPassword.executeOnError")
    public Mono<ResponseEntity<Void>> executeOnError(String userId, String adminAccessToken, Throwable e) {
        return webClient.delete()
                .uri(userByIdUrl, userId)
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + adminAccessToken)
                .retrieve()
                .toBodilessEntity()
                .then(Mono.error(e));
    }

    private static void addIfNotBlank(LinkedMultiValueMap<String, String> form, String key, String value) {
        if (value != null && !value.isBlank()) form.add(key, value);
    }

    private Mono<? extends Throwable> toApiException(ClientResponse resp) {
        return resp.bodyToMono(String.class)
                .defaultIfEmpty(resp.statusCode().toString())
                .map(body -> new ApiException("Keycloak error " + resp.statusCode() + ": " + body));
    }
}
