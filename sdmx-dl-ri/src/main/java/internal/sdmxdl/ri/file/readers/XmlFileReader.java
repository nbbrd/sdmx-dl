package internal.sdmxdl.ri.file.readers;

import internal.sdmxdl.ri.file.CachedResource;
import internal.sdmxdl.ri.file.SdmxFileConnectionImpl;
import internal.sdmxdl.ri.file.StaxSdmxDecoder;
import nbbrd.service.ServiceProvider;
import sdmxdl.DataStructureRef;
import sdmxdl.Dataflow;
import sdmxdl.file.SdmxFileConnection;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.file.spi.SdmxFileContext;
import sdmxdl.file.spi.SdmxFileReader;
import sdmxdl.util.parser.ObsFactories;
import sdmxdl.xml.XmlFileSource;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

@ServiceProvider(SdmxFileReader.class)
public class XmlFileReader implements SdmxFileReader {

    @Override
    public SdmxFileSource getSource(String name) {
        try {
            return XmlFileSource.getParser().parseChars(name);
        } catch (IOException ex) {
            return null;
        }
    }

    @Override
    public boolean canRead(SdmxFileSource source) {
        return isXmlFileName(source.getData());
    }

    @Override
    public SdmxFileConnection read(SdmxFileSource source, SdmxFileContext context) throws IOException, IllegalArgumentException {
        Objects.requireNonNull(source);
        Objects.requireNonNull(context);
        if (!canRead(source)) {
            throw new IllegalArgumentException(source.toString());
        }
        return new SdmxFileConnectionImpl(getResource(source, context), getDataflow(source));
    }

    private boolean isXmlFileName(File file) {
        return file.toString().toLowerCase(Locale.ROOT).endsWith(".xml");
    }

    private SdmxFileConnectionImpl.Resource getResource(SdmxFileSource source, SdmxFileContext context) throws IOException {
        return new CachedResource(source, context.getLanguages(), new StaxSdmxDecoder(), ObsFactories.getObsFactory(context, source), context.getCache());
    }

    private static final DataStructureRef EMPTY = DataStructureRef.of("", "", "");

    private Dataflow getDataflow(SdmxFileSource source) {
        return Dataflow.of(source.asDataflowRef(), EMPTY, SdmxFileSource.asFlowLabel(source));
    }
}
