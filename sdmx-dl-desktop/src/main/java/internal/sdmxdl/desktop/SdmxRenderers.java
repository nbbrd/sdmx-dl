package internal.sdmxdl.desktop;

import internal.sdmxdl.desktop.util.Ikons;
import internal.sdmxdl.desktop.util.UIConstants;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import sdmxdl.Languages;
import sdmxdl.desktop.DataSetRef;
import sdmxdl.desktop.JDataSet;
import sdmxdl.desktop.JDriver;
import sdmxdl.desktop.JWebSource;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Driver;

import javax.swing.*;

public final class SdmxRenderers {

    private SdmxRenderers() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static String webSourceToHeader(WebSource value) {
        return "<html>" + value.getId();
    }

    public static String webSourceToText(WebSource value) {
        return "<html><a href='#'>[" + value.getId() + "]</a> " + value.getName(Languages.ANY);
    }

    public static Icon webSourceToIcon(WebSource value, SdmxIconSupport support, Runnable onUpdate) {
        return support.getIcon(value.getId(), 16, onUpdate);
    }

    public static JComponent webSourceToView(WebSource value) {
        JWebSource result = new JWebSource();
        result.setModel(value);
        return result;
    }

    public static String driverToHeader(Driver value) {
        return value.getDriverId();
    }

    public static String driverToText(Driver value) {
        return value.getDriverId();
    }

    public static Icon driverToIcon(Driver value) {
        return Ikons.of(MaterialDesign.MDI_CHIP, 16, UIConstants.TREE_ICON_LEAF_COLOR);
    }

    public static JComponent driverToView(Driver value) {
        JDriver result = new JDriver();
        result.setModel(value);
        return result;
    }

    public static String dataSetRefToHeader(DataSetRef value) {
        return "<html>" + value.getDataSourceRef().getFlow().getId() + "/" + value.getKey();
    }

    public static Icon dataSetRefToIcon(DataSetRef value, SdmxIconSupport support, Runnable onUpdate) {
        return support.getIcon(value.getDataSourceRef(), 16, onUpdate);
    }

    public static JComponent dataSetRefToView(DataSetRef value, SdmxWebManager manager) {
        JDataSet result = new JDataSet();
        result.setSdmxManager(manager);
        result.setModel(value);
        return result;
    }
}
