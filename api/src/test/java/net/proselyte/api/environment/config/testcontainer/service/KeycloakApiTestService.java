package net.proselyte.api.environment.config.testcontainer.service;

import lombok.RequiredArgsConstructor;

import net.proselyte.api.config.KeycloakProperties;
import net.proselyte.api.exception.ApiException;
import org.keycloak.admin.client.Keycloak;

import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;


import net.proselyte.individual.dto.UserLoginRequest;

@Service
@RequiredArgsConstructor
public class KeycloakApiTestService {

    private final Keycloak keycloak;
    private final KeycloakProperties keycloakProperties;
    private final IndividualApiTestService individualApiTestService;

    public UserRepresentation getUserRepresentation(String email) {
        var users = keycloak.realm(keycloakProperties.realm()).users().list();

        for (var user : users) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }

        throw new ApiException("User not found by email=[ %s ]", email);
    }

    public void clear() {
        var users = keycloak.realm(keycloakProperties.realm()).users().list();
        for (var user : users) {
            if (!keycloakProperties.adminUsername().equals(user.getUsername()))    {
                keycloak.realm(keycloakProperties.realm()).users().delete(user.getId());
            }
        }
    }

    public String getAdminAccessToken() {
        return individualApiTestService.login(new UserLoginRequest(keycloakProperties.adminUsername(), keycloakProperties.adminPassword()))
            .getAccessToken();
    }
}
