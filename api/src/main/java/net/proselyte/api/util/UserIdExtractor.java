package net.proselyte.api.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UserIdExtractor {

    public static final String REGEX_GET_SUBSTRING_AFTER_LAST_SLASH = ".*/([^/+])";

    public static String extractIdFromPath(String path) {
        return path.replaceAll(REGEX_GET_SUBSTRING_AFTER_LAST_SLASH, "$1");
    }
}
