package sdmxdl.format.xml;

import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import sdmxdl.format.spi.Persistence;
import sdmxdl.format.spi.PersistenceSupport;

@DirectImpl
@ServiceProvider
public final class XmlPersistence implements Persistence {

    @lombok.experimental.Delegate
    private final PersistenceSupport persistence = PersistenceSupport
            .builder()
            .id("XML")
            .rank(100)
            .sources(XmlWebSourcesFormat.INSTANCE)
            .build();
}
