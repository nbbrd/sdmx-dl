package sdmxdl.desktop.panels;

import ec.util.list.swing.JLists;
import ec.util.table.swing.JTables;
import internal.sdmxdl.desktop.util.Ikons;
import internal.sdmxdl.desktop.util.JDocument;
import internal.sdmxdl.desktop.util.JEditorTabs;
import org.kordamp.ikonli.Ikon;
import sdmxdl.desktop.MainComponent;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;

public interface Renderer<T> {


    String toText(T value, Runnable onUpdate);

    default Icon toIcon(T value, Runnable onUpdate) {
        return null;
    }

    default String toTooltip(T value, Runnable onUpdate) {
        return null;
    }

    default String toHeaderText(T value, Runnable onUpdate) {
        return toText(value, onUpdate);
    }

    default Icon toHeaderIcon(T value, Runnable onUpdate) {
        return toIcon(value, onUpdate);
    }

    default String toHeaderTooltip(T value, Runnable onUpdate) {
        return toTooltip(value, onUpdate);
    }

    default JDocument<T> toView(MainComponent main, T value) {
        JDocument<T> result = new JDocument<>();
        result.addComponent(toHeaderText(value, null), new JLabel(toText(value, null), toIcon(value, null), JLabel.LEADING));
        result.setModel(value);
        return result;
    }

    default ListCellRenderer<T> asListCellRenderer(Runnable onUpdate) {
        return JLists.cellRendererOf((label, value) -> render(label, value, onUpdate));
    }

    default TableCellRenderer asTableCellRenderer(Runnable onUpdate) {
        return JTables.<T>cellRendererOf((label, value) -> render(label, value, onUpdate));
    }

    default void render(JLabel label, T value, Runnable onUpdate) {
        label.setText(toText(value, onUpdate));
        label.setIcon(toIcon(value, onUpdate));
        label.setToolTipText(toTooltip(value, onUpdate));
    }

    default JEditorTabs.TabFactory<T> asTabFactory(MainComponent main) {
        return JEditorTabs.TabFactorySupport
                .<T>builder()
                .titleFactory((id, source) -> toHeaderText(id, source::repaint))
                .iconFactory((id, source) -> toHeaderIcon(id, source::repaint))
                .componentFactory((id, source) -> toView(main, id))
                .tipFactory((id, source) -> toHeaderTooltip(id, source::repaint))
                .build();
    }

    static Icon getIcon(Ikon ikon) {
        return Ikons.of(ikon, 16, "Tree.icon.leafColor");
    }
}
