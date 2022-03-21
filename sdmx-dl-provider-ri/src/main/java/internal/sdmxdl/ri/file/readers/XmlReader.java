package internal.sdmxdl.ri.file.readers;

import internal.sdmxdl.ri.file.XmlDecoder;
import internal.sdmxdl.ri.file.XmlFileClient;
import lombok.NonNull;
import nbbrd.service.ServiceProvider;
import sdmxdl.Connection;
import sdmxdl.DataStructureRef;
import sdmxdl.Dataflow;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.file.spi.FileContext;
import sdmxdl.file.spi.FileReader;
import sdmxdl.util.file.CachedFileClient;
import sdmxdl.util.file.FileConnectionImpl;
import sdmxdl.util.file.SdmxFileClient;
import sdmxdl.util.parser.DefaultObsParser;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

@ServiceProvider
public class XmlReader implements FileReader {

    @Override
    public boolean canRead(@NonNull SdmxFileSource source) {
        return isXmlFileName(source.getData());
    }

    @Override
    public @NonNull Connection read(@NonNull SdmxFileSource source, @NonNull FileContext context) throws IOException, IllegalArgumentException {
        if (!canRead(source)) {
            throw new IllegalArgumentException(source.toString());
        }
        return new FileConnectionImpl(getClient(source, context), getDataflow(source));
    }

    private boolean isXmlFileName(File file) {
        return file.toString().toLowerCase(Locale.ROOT).endsWith(".xml");
    }

    private SdmxFileClient getClient(SdmxFileSource source, FileContext context) throws IOException {
        SdmxFileClient client = new XmlFileClient(
                source,
                context.getLanguages(),
                new XmlDecoder(context.getEventListener()),
                DefaultObsParser::newDefault,
                context.getEventListener()
        );
        return CachedFileClient.of(client, context.getCache(), source, context.getLanguages());
    }

    private static final DataStructureRef EMPTY = DataStructureRef.of("", "", "");

    private Dataflow getDataflow(SdmxFileSource source) {
        return Dataflow.of(source.asDataflowRef(), EMPTY, SdmxFileSource.asFlowLabel(source));
    }
}
