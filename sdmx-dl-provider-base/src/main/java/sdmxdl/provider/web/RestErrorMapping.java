package sdmxdl.provider.web;

import lombok.NonNull;

import java.util.stream.Stream;

/**
 * see https://sdmx.org/wp-content/uploads/SDMX_2-1-1-SECTION_07_WebServicesGuidelines_2013-04.pdf
 * 5.8 SDMX to HTTP Error Mapping
 */
@lombok.AllArgsConstructor
@lombok.Getter
public enum RestErrorMapping {

    CLIENT_NO_RESULTS_FOUND(100, 404),
    CLIENT_UNAUTHORIZED(110, 401),
    CLIENT_RESPONSE_TOO_LARGE(130, 413),
    CLIENT_SYNTAX_ERROR(140, 400),
    CLIENT_SEMANTIC_ERROR(150, 400),
    SERVER_INTERNAL_SERVER_ERROR(500, 500),
    SERVER_NOT_IMPLEMENTED(501, 501),
    SERVER_SERVICE_UNAVAILABLE(503, 503),
    SERVER_RESPONSE_SIZE_EXCEEDS_LIMIT(510, 413),
    UNDEFINED(0, 0);

    final int sdmxCode;

    final int httpCode;

    public static @NonNull RestErrorMapping getByHttpCode(int code) {
        return Stream.of(RestErrorMapping.values())
                .filter(value -> value.httpCode == code)
                .findFirst()
                .orElse(UNDEFINED);
    }
}
