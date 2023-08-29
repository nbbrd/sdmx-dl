package sdmxdl.provider.ri.readers;

import nbbrd.design.DirectImpl;
import lombok.NonNull;
import nbbrd.service.ServiceProvider;
import sdmxdl.Connection;
import sdmxdl.DataStructureRef;
import sdmxdl.Dataflow;
import sdmxdl.Languages;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.file.spi.FileContext;
import sdmxdl.file.spi.Reader;
import sdmxdl.format.ObsParser;
import sdmxdl.provider.file.CachedFileClient;
import sdmxdl.provider.file.FileClient;
import sdmxdl.provider.file.FileConnection;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

@DirectImpl
@ServiceProvider
public final class XmlReader implements Reader {

    @Override
    public @NonNull String getReaderId() {
        return "XML";
    }

    @Override
    public int getReaderRank() {
        return UNKNOWN_READER_RANK;
    }

    @Override
    public boolean isReaderAvailable() {
        return true;
    }

    @Override
    public boolean canRead(@NonNull SdmxFileSource source) {
        return isXmlFileName(source.getData());
    }

    @Override
    public @NonNull Connection read(@NonNull SdmxFileSource source, @NonNull Languages languages, @NonNull FileContext context) throws IOException, IllegalArgumentException {
        if (!canRead(source)) {
            throw new IllegalArgumentException(source.toString());
        }
        return new FileConnection(getClient(source, languages, context), getDataflow(source));
    }

    private boolean isXmlFileName(File file) {
        return file.toString().toLowerCase(Locale.ROOT).endsWith(".xml");
    }

    private FileClient getClient(SdmxFileSource source, Languages languages, FileContext context) throws IOException {
        FileClient client = new XmlFileClient(
                source,
                languages,
                new XmlDecoder(context.getOnEvent()),
                ObsParser::newDefault,
                context.getOnEvent()
        );
        return CachedFileClient.of(client, context.getReaderCache(source), source, languages);
    }

    private static final DataStructureRef EMPTY = DataStructureRef.of("", "", "");

    private Dataflow getDataflow(SdmxFileSource source) {
        return Dataflow.builder().ref(source.asDataflowRef()).structureRef(EMPTY).name(SdmxFileSource.asFlowLabel(source)).build();
    }
}
