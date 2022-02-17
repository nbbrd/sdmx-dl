package sdmxdl.tck.web;

import sdmxdl.SdmxConnection;
import sdmxdl.web.SdmxWebConnection;

@lombok.RequiredArgsConstructor
public class MockedWebConnection implements SdmxWebConnection {

    @lombok.Getter
    private final String driver;

    @lombok.experimental.Delegate
    private final SdmxConnection delegate;
}
