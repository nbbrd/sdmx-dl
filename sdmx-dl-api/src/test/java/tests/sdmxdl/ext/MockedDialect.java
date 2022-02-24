package tests.sdmxdl.ext;

import sdmxdl.ext.ObsFactory;
import sdmxdl.ext.spi.Dialect;

@lombok.RequiredArgsConstructor
public final class MockedDialect implements Dialect {

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
