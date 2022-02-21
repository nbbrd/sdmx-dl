package tests.sdmxdl.ext;

import sdmxdl.ext.ObsFactory;
import sdmxdl.ext.spi.SdmxDialect;

@lombok.RequiredArgsConstructor
public final class MockedDialect implements SdmxDialect {

    @lombok.Getter
    private final String name;

    @Override
    public String getDescription() {
        return getName();
    }

    @Override
    public ObsFactory getObsFactory() {
        return dsd -> null;
    }
}
