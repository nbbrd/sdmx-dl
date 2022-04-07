package internal.sdmxdl.provider.ri.web.monitors;

import lombok.NonNull;
import nbbrd.design.RepresentableAs;
import nbbrd.design.StaticFactoryMethod;

import java.net.URI;

@RepresentableAs(URI.class)
@lombok.Value
@lombok.Builder
class UptimeRobotId {

    public static final String URI_SCHEME = "uptimerobot";

    @lombok.NonNull
    String apiKey;

    @lombok.Builder.Default
    boolean logs = false;

    @lombok.Builder.Default
    boolean allTimeUptimeRatio = false;

    @lombok.Builder.Default
    boolean responseTimesAverage = false;

    @NonNull
    public String toBody() {
        return "api_key=" + apiKey
                + "&format=" + "xml"
                + "&logs=" + format(logs)
                + "&all_time_uptime_ratio=" + format(allTimeUptimeRatio)
                + "&response_times_average=" + format(responseTimesAverage);
    }

    private String format(boolean value) {
        return value ? "1" : "0";
    }

    public @NonNull URI toURI() {
        return URI.create(URI_SCHEME + ":" + apiKey);
    }

    @StaticFactoryMethod
    public static @NonNull UptimeRobotId parse(@NonNull URI uri) {
        if (!uri.getScheme().equals(URI_SCHEME)) {
            throw new IllegalArgumentException("Invalid scheme");
        }
        return UptimeRobotId
                .builder()
                .apiKey(uri.toString().substring(URI_SCHEME.length() + 1))
                .allTimeUptimeRatio(true)
                .responseTimesAverage(false)
                .build();
    }
}
