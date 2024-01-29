package sdmxdl.desktop;

import ec.util.list.swing.JLists;
import internal.sdmxdl.desktop.PropertyFormats;
import internal.sdmxdl.desktop.util.*;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import sdmxdl.Languages;
import sdmxdl.ext.Persistence;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.*;

import javax.swing.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Optional;

import static org.kordamp.ikonli.materialdesign.MaterialDesign.MDI_WEB;

public interface Renderer<T> {

    String toHeader(T value);

    String toText(T value);

    Icon toIcon(T value, Runnable onUpdate);

    JComponent toView(T value);

    default ListCellRenderer<T> asListCellRenderer(Runnable onUpdate) {
        return JLists.cellRendererOf((label, value) -> render(label, value, onUpdate));
    }

    default void render(JLabel label, T value) {
        render(label, value, null);
    }

    default void render(JLabel label, T value, Runnable onUpdate) {
        label.setText(toText(value));
        label.setIcon(toIcon(value, onUpdate));
    }

    default JEditorTabs.TabFactory<T> asTabFactory() {
        return JEditorTabs.TabFactorySupport
                .<T>builder()
                .titleFactory((id, source) -> toHeader(id))
                .iconFactory((id, source) -> toIcon(id, source::repaint))
                .componentFactory((id, source) -> toView(id))
                .tipFactory((id, source) -> toText(id))
                .build();
    }

    Renderer<WebSource> WEB_SOURCE_RENDERER = new Renderer<WebSource>() {
        @Override
        public String toHeader(WebSource value) {
            return value.getId();
        }

        @Override
        public String toText(WebSource value) {
            return "<html><a href='#'>[" + value.getId() + "]</a> " + value.getName(Languages.ANY);
        }

        @Override
        public Icon toIcon(WebSource value, Runnable onUpdate) {
            return Sdmxdl.INSTANCE.getIconSupport().getIcon(value.getId(), 16, onUpdate);
        }

        @Override
        public JComponent toView(WebSource value) {
            JDocument<WebSource> result = new JDocument<>();
            result.addComponent("Settings", new JWebSourceSettings(), JWebSourceSettings::setModel);
            result.addComponent("Explorer", new JLabel("TODO"));
            result.addToolBarItem(new ButtonBuilder()
                    .action(BrowseCommand.ofURL(this::getWebsite)
                            .toAction(result)
                            .withWeakPropertyChangeListener(result, JDocument.MODEL_PROPERTY))
                    .ikon(MDI_WEB)
                    .toolTipText("Open web site")
                    .build());
            result.setModel(value);
            return result;
        }

        public URL getWebsite(JDocument<WebSource> o) {
            return Optional.ofNullable(o.getModel()).map(WebSource::getWebsite).orElse(null);
        }
    };

    Renderer<String> PROPERTY_RENDERER = new Renderer<String>() {
        @Override
        public String toHeader(String value) {
            return value;
        }

        @Override
        public String toText(String value) {
            return PropertyFormats.toText(value);
        }

        @Override
        public Icon toIcon(String value, Runnable onUpdate) {
            return null;
        }

        @Override
        public JComponent toView(String value) {
            return null;
        }
    };

    Renderer<Driver> DRIVER_RENDERER = new Renderer<Driver>() {
        @Override
        public String toHeader(Driver value) {
            return value.getDriverId();
        }

        @Override
        public String toText(Driver value) {
            return value.getDriverId();
        }

        @Override
        public Icon toIcon(Driver value, Runnable onUpdate) {
            return Ikons.of(MaterialDesign.MDI_CHIP, 16, UIConstants.TREE_ICON_LEAF_COLOR);
        }

        @Override
        public JComponent toView(Driver value) {
            return HasModel.create(JDriver::new, value);
        }
    };

    Renderer<DataSetRef> DATA_SET_REF_RENDERER = new Renderer<DataSetRef>() {
        @Override
        public String toHeader(DataSetRef value) {
            return value.getDataSourceRef().getFlow().getId() + "/" + value.getKey();
        }

        @Override
        public String toText(DataSetRef value) {
            return toHeader(value);
        }

        @Override
        public Icon toIcon(DataSetRef value, Runnable onUpdate) {
            return Sdmxdl.INSTANCE.getIconSupport().getIcon(value.getDataSourceRef(), 16, onUpdate);
        }

        @Override
        public JComponent toView(DataSetRef value) {
            return HasModel.create(JDataSet::new, value);
        }
    };

    Renderer<WebCaching> WEB_CACHING_RENDERER = new Renderer<WebCaching>() {
        @Override
        public String toHeader(WebCaching value) {
            return value.getWebCachingId();
        }

        @Override
        public String toText(WebCaching value) {
            return value.getWebCachingId();
        }

        @Override
        public Icon toIcon(WebCaching value, Runnable onUpdate) {
            return Ikons.of(MaterialDesign.MDI_FILE_CLOUD, 16, UIConstants.TREE_ICON_LEAF_COLOR);
        }

        @Override
        public JComponent toView(WebCaching value) {
            JPluginProperties<WebCaching> result = new JPluginProperties<>();
            result.setModel(value);
            result.setExtractor(WebCaching::getWebCachingProperties);
            return result;
        }
    };

    Renderer<Authenticator> AUTHENTICATOR_RENDERER = new Renderer<Authenticator>() {
        @Override
        public String toHeader(Authenticator value) {
            return value.getAuthenticatorId();
        }

        @Override
        public String toText(Authenticator value) {
            return value.getAuthenticatorId();
        }

        @Override
        public Icon toIcon(Authenticator value, Runnable onUpdate) {
            return Ikons.of(MaterialDesign.MDI_ACCOUNT_KEY, 16, UIConstants.TREE_ICON_LEAF_COLOR);
        }

        @Override
        public JComponent toView(Authenticator value) {
            return new JLabel(toText(value));
        }
    };

    Renderer<Monitor> MONITOR_RENDERER = new Renderer<Monitor>() {
        @Override
        public String toHeader(Monitor value) {
            return value.getMonitorId();
        }

        @Override
        public String toText(Monitor value) {
            return value.getMonitorId();
        }

        @Override
        public Icon toIcon(Monitor value, Runnable onUpdate) {
            return Ikons.of(MaterialDesign.MDI_GAUGE, 16, UIConstants.TREE_ICON_LEAF_COLOR);
        }

        @Override
        public JComponent toView(Monitor value) {
            return new JLabel(toText(value));
        }
    };

    Renderer<Persistence> PERSISTENCE_RENDERER = new Renderer<Persistence>() {
        @Override
        public String toHeader(Persistence value) {
            return value.getPersistenceId();
        }

        @Override
        public String toText(Persistence value) {
            return value.getPersistenceId();
        }

        @Override
        public Icon toIcon(Persistence value, Runnable onUpdate) {
            return Ikons.of(MaterialDesign.MDI_CONTENT_SAVE, 16, UIConstants.TREE_ICON_LEAF_COLOR);
        }

        @Override
        public JComponent toView(Persistence value) {
            return new JLabel(toText(value));
        }
    };

    Renderer<Registry> REGISTRY_RENDERER = new Renderer<Registry>() {
        @Override
        public String toHeader(Registry value) {
            return value.getRegistryId();
        }

        @Override
        public String toText(Registry value) {
            return value.getRegistryId();
        }

        @Override
        public Icon toIcon(Registry value, Runnable onUpdate) {
            return Ikons.of(MaterialDesign.MDI_SITEMAP, 16, UIConstants.TREE_ICON_LEAF_COLOR);
        }

        @Override
        public JComponent toView(Registry value) {
            JPluginProperties<Registry> result = new JPluginProperties<>();
            result.setModel(value);
            result.setExtractor(Registry::getRegistryProperties);
            return result;
        }
    };

    Renderer<Networking> NETWORKING_RENDERER = new Renderer<Networking>() {
        @Override
        public String toHeader(Networking value) {
            return value.getNetworkingId();
        }

        @Override
        public String toText(Networking value) {
            return value.getNetworkingId();
        }

        @Override
        public Icon toIcon(Networking value, Runnable onUpdate) {
            return Ikons.of(MaterialDesign.MDI_ACCESS_POINT_NETWORK, 16, UIConstants.TREE_ICON_LEAF_COLOR);
        }

        @Override
        public JComponent toView(Networking value) {
            JPluginProperties<Networking> result = new JPluginProperties<>();
            result.setModel(value);
            result.setExtractor(Networking::getNetworkingProperties);
            return result;
        }
    };

    Renderer<SwingWorker<?, ?>> SWING_WORKER_RENDERER = new Renderer<SwingWorker<?, ?>>() {
        @Override
        public String toHeader(SwingWorker<?, ?> value) {
            return "Loading";
        }

        @Override
        public String toText(SwingWorker<?, ?> value) {
            return toHeader(value);
        }

        @Override
        public Icon toIcon(SwingWorker<?, ?> value, Runnable onUpdate) {
            return Ikons.of(MaterialDesign.MDI_CLOUD_DOWNLOAD, 16, UIConstants.TREE_ICON_LEAF_COLOR);
        }

        @Override
        public JComponent toView(SwingWorker<?, ?> value) {
            return new JLabel(toText(value));
        }
    };

    Renderer<Exception> ERROR_RENDERER = new Renderer<Exception>() {
        @Override
        public String toHeader(Exception value) {
            return "Error " + value.getClass().getSimpleName();
        }

        @Override
        public String toText(Exception value) {
            return toHeader(value);
        }

        @Override
        public Icon toIcon(Exception value, Runnable onUpdate) {
            return Ikons.of(MaterialDesign.MDI_CLOSE_NETWORK, 16, UIConstants.TREE_ICON_LEAF_COLOR);
        }

        @Override
        public JComponent toView(Exception value) {
            StringWriter writer = new StringWriter();
            value.printStackTrace(new PrintWriter(writer));
            return new JScrollPane(new JTextArea(writer.toString()));
        }
    };
}
