package net.proselyte.api.spec.integration.controller;

import net.proselyte.api.spec.integration.LifecycleSpecification;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthRestControllerV1Test extends LifecycleSpecification {

    @Test
    void shouldCreateNewUserAndReturnAccessToken() {
        // when
        var request = dtoCreator.buildIndividualWriteDto();
        var response = individualControllerService.register(request);
        var meResponse = individualControllerService.getMe(response.getAccessToken());

        var personId = keycloakApiTestService
                .getUserRepresentation(request.getEmail())
                .getId();

        // then
        assertTrue(StringUtils.isNoneBlank(personId));
        assertNotNull(response, "Response must not be null");
        assertNotNull(response.getAccessToken(), "Access token must not be null");
        assertEquals("Bearer", response.getTokenType(), "Token type must be Bearer");
        assertEquals(request.getEmail(), meResponse.getEmail());
    }

    @Test
    void shouldLoginAndReturnAccessToken() {
        // given: регистрируем пользователя
        var registerRequest = dtoCreator.buildIndividualWriteDto();
        individualControllerService.register(registerRequest);

        // when: логинимся тем же email/password
        var loginRequest = dtoCreator.buildUserLoginRequest();
        var response = individualControllerService.login(loginRequest);
        var meResponse = individualControllerService.getMe(response.getAccessToken());

        // then
        assertNotNull(response, "Response must not be null");
        assertNotNull(response.getAccessToken(), "Access token must not be null");
        assertEquals("Bearer", response.getTokenType(), "Token type must be Bearer");
        assertEquals(registerRequest.getEmail(), meResponse.getEmail());
    }

    @Test
    void shouldReturnUserInfo() {
        // given
        var individualWriteDto = dtoCreator.buildIndividualWriteDto();
        var registrationResponse = individualControllerService.register(individualWriteDto);

        // when
        var meResponse = individualControllerService.getMe(registrationResponse.getAccessToken());

        // then
        assertNotNull(meResponse.getEmail(), "email in /me must be present");
        assertEquals(individualWriteDto.getEmail(), meResponse.getEmail(), "emails must match");
    }
}
