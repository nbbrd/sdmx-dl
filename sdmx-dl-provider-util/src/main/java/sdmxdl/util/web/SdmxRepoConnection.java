package sdmxdl.util.web;

import sdmxdl.SdmxConnection;
import sdmxdl.web.SdmxWebConnection;

import java.time.Duration;

@lombok.RequiredArgsConstructor
final class SdmxRepoConnection implements SdmxWebConnection {

    @lombok.Getter
    private final String driver;

    @lombok.experimental.Delegate
    private final SdmxConnection delegate;

    @Override
    public Duration ping() {
        return Duration.ZERO;
    }
}
