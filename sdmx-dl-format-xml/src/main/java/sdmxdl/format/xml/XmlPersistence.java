package sdmxdl.format.xml;

import lombok.experimental.Delegate;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import sdmxdl.HasPersistence;
import sdmxdl.ext.FileFormat;
import sdmxdl.ext.Persistence;
import sdmxdl.format.PersistenceSupport;
import sdmxdl.web.WebSources;

@DirectImpl
@ServiceProvider
public final class XmlPersistence implements Persistence {

    @Delegate
    private final PersistenceSupport persistence = PersistenceSupport
            .builder()
            .id("XML")
            .rank(100)
            .support(WebSources.class::equals)
            .factory(XmlPersistence::create)
            .build();

    @SuppressWarnings("unchecked")
    private static <T extends HasPersistence> FileFormat<T> create(Class<T> type) {
        if (WebSources.class.equals(type)) {
            return (FileFormat<T>) XmlWebSourcesFormat.INSTANCE;
        }
        return FileFormat.noOp();
    }
}
