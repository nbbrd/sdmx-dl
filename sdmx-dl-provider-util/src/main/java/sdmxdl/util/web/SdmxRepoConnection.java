package sdmxdl.util.web;

import sdmxdl.SdmxConnection;
import sdmxdl.web.SdmxWebConnection;

@lombok.RequiredArgsConstructor
final class SdmxRepoConnection implements SdmxWebConnection {

    @lombok.Getter
    private final String driver;

    @lombok.experimental.Delegate
    private final SdmxConnection delegate;
}
