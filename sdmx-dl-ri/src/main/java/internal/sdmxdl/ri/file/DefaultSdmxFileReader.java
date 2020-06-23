package internal.sdmxdl.ri.file;

import nbbrd.service.ServiceProvider;
import sdmxdl.DataStructureRef;
import sdmxdl.Dataflow;
import sdmxdl.ext.ObsFactory;
import sdmxdl.file.SdmxFileConnection;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.file.spi.SdmxFileContext;
import sdmxdl.file.spi.SdmxFileReader;
import sdmxdl.xml.XmlFileSource;

import java.io.IOException;
import java.util.Optional;

@ServiceProvider(SdmxFileReader.class)
public class DefaultSdmxFileReader implements SdmxFileReader {

    @Override
    public SdmxFileSource getSource(String name) {
        try {
            return XmlFileSource.fromXml(name);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    @Override
    public boolean canRead(SdmxFileSource source) {
        return true;
    }

    @Override
    public SdmxFileConnection read(SdmxFileSource source, SdmxFileContext context) throws IOException {
        return new SdmxFileConnectionImpl(getResource(source, context), getDataflow(source));
    }

    private SdmxFileConnectionImpl.Resource getResource(SdmxFileSource source, SdmxFileContext context) {
        return new CachedResource(source, context.getLanguages(), new StaxSdmxDecoder(), getDataFactory(source, context), context.getCache());
    }

    private static final DataStructureRef EMPTY = DataStructureRef.of("", "", "");

    private Dataflow getDataflow(SdmxFileSource source) {
        return Dataflow.of(source.asDataflowRef(), EMPTY, SdmxFileSource.asFlowLabel(source));
    }

    private Optional<ObsFactory> getDataFactory(SdmxFileSource source, SdmxFileContext context) {
        return context.getDialects().stream()
                .filter(o -> o.getName().equals(source.getDialect()))
                .findFirst()
                .map(ObsFactory.class::cast);
    }
}
