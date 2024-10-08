package sdmxdl.desktop;

import internal.sdmxdl.desktop.SdmxIconSupport;
import internal.sdmxdl.desktop.util.AsyncSupport;
import lombok.Getter;
import lombok.NonNull;
import sdmxdl.HasPersistence;
import sdmxdl.Languages;
import sdmxdl.ext.FileFormat;
import sdmxdl.web.SdmxWebManager;

import java.beans.PropertyChangeSupport;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

public enum Sdmxdl implements HasSdmxProperties<SdmxWebManager> {

    INSTANCE;

    private final PropertyChangeSupport broadcaster = new PropertyChangeSupport(this);

    @Getter
    private SdmxWebManager sdmxManager = SdmxWebManager.noOp();

    public void setSdmxManager(@NonNull SdmxWebManager sdmxManager) {
        broadcaster.firePropertyChange(SDMX_MANAGER_PROPERTY, this.sdmxManager, this.sdmxManager = sdmxManager);
    }

    @Getter
    private Languages languages = Languages.ANY;

    public void setLanguages(@NonNull Languages languages) {
        broadcaster.firePropertyChange(LANGUAGES_PROPERTY, this.languages, this.languages = languages);
    }

    @Getter
    private final SdmxIconSupport iconSupport = SdmxIconSupport.of(this);

    @Getter
    private final AsyncSupport<DataSourceRef, FlowStruct> flowStructSupport = AsyncSupport.of(ref -> FlowStruct.load(this.getSdmxManager(), ref));

    public <T extends HasPersistence> String formatAsJson(Class<T> type, T value) {
        Optional<FileFormat<T>> dsdFormat = getSdmxManager()
                .getPersistences()
                .stream()
                .filter(persistence -> persistence.getFormatSupportedTypes().contains(type))
                .map(persistence -> persistence.getFormat(type))
                .filter(format -> format.getFileExtension().equals(".json"))
                .findFirst();
        if (dsdFormat.isPresent()) {
            try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
                dsdFormat.get().formatStream(value, stream);
                return new String(stream.toByteArray(), UTF_8);
            } catch (IOException ex) {
                return ex.getMessage();
            }
        } else {
            return "Cannot format DSD";
        }
    }
}
