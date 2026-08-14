package internal.sdmxdl.provider.px.drivers;

import lombok.NonNull;
import sdmxdl.Database;
import sdmxdl.Flow;
import sdmxdl.Key;
import sdmxdl.Structure;
import sdmxdl.format.DataCursor;
import sdmxdl.provider.HasMarker;

import java.io.IOException;
import java.net.URI;
import java.util.List;

public interface PxWebClient extends HasMarker {

    @NonNull
    URI ping() throws IOException;

    @NonNull
    List<Database> getDataBases() throws IOException;

    @NonNull
    List<Flow> getTables(@NonNull String dbId) throws IOException;

    @NonNull
    Structure getMeta(@NonNull String dbId, @NonNull String tablePath) throws IOException, IllegalArgumentException;

    @NonNull
    DataCursor getData(@NonNull String dbId, @NonNull String tablePath, @NonNull Structure dsd, @NonNull Key key) throws IOException, IllegalArgumentException;
}
