package sdmxdl.format.kryo;

import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import sdmxdl.DataRepository;
import sdmxdl.format.WebSources;
import sdmxdl.format.spi.Persistence;
import sdmxdl.format.spi.PersistenceSupport;
import sdmxdl.web.MonitorReports;

@DirectImpl
@ServiceProvider
public final class KryoPersistence implements Persistence {

    @lombok.experimental.Delegate
    private final PersistenceSupport persistence = PersistenceSupport
            .builder()
            .id("KRYO")
            .rank(400)
            .monitor(new KryoFileFormat<>(MonitorReports.class))
            .repository(new KryoFileFormat<>(DataRepository.class))
            .sources(new KryoFileFormat<>(WebSources.class))
            .build();
}
