package tests.sdmxdl.web;

import sdmxdl.SdmxConnection;
import sdmxdl.web.SdmxWebConnection;

@lombok.RequiredArgsConstructor
public final class MockedWebConnection implements SdmxWebConnection {

    @lombok.Getter
    private final String driver;

    @lombok.experimental.Delegate
    private final SdmxConnection delegate;
}
