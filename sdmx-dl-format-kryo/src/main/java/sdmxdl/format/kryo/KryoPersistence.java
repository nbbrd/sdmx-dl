package sdmxdl.format.kryo;

import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import sdmxdl.ext.Persistence;
import sdmxdl.format.PersistenceSupport;

@DirectImpl
@ServiceProvider
public final class KryoPersistence implements Persistence {

    @lombok.experimental.Delegate
    private final PersistenceSupport persistence = PersistenceSupport
            .builder()
            .id("KRYO")
            .rank(400)
            .support(ignore -> true)
            .factory(KryoFileFormat::new)
            .build();
}
