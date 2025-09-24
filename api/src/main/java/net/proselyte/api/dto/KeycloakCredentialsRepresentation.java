package net.proselyte.api.dto;

public record KeycloakCredentialsRepresentation(
        String type,
        String value,
        Boolean temporary
) {
}
