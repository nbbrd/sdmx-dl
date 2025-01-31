package internal.sdmxdl.desktop.util;

import j2html.TagCreator;
import j2html.tags.DomContent;
import j2html.tags.UnescapedText;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

import static j2html.TagCreator.*;

public final class OnDemandMenuBuilder {

    private final JMenu result = new JMenu();

    public OnDemandMenuBuilder copyToClipboard(String textMenu, String clipboardContent) {
        addCopyToClipboard(result, textMenu, clipboardContent);
        return this;
    }

    public static void addCopyToClipboard(JMenu menu, String textMenu, String clipboardContent) {
        JMenuItem item = menu.add(copyToClipboard(clipboardContent));
        item.setText(html(text(textMenu), SEPARATOR, preview(clipboardContent)).render());
        item.setToolTipText(html(clipboardContent).render());
    }

    public OnDemandMenuBuilder openFolder(String textMenu, File folder) {
        addOpenFolder(result, textMenu, folder);
        return this;
    }

    public static void addOpenFolder(JMenu menu, String textMenu, File folder) {
        JMenuItem item = menu.add(openFolder(folder));
        item.setText(html(text(textMenu), SEPARATOR, preview(folder.getPath())).render());
        item.setToolTipText(html(folder.getPath()).render());
    }

    public OnDemandMenuBuilder addSeparator() {
        result.addSeparator();
        return this;
    }

    public JMenu build() {
        return result;
    }

    public void showMenuAsPopup(Component invoker) {
        showMenuAsPopup(invoker, result);
    }

    public static void showMenuAsPopup(Component invoker, JMenu menu) {
        if (invoker == null)
            invoker = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        Point mousePosition = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(mousePosition, invoker);
        menu.getPopupMenu().show(invoker, mousePosition.x, mousePosition.y);
    }

    private static DomContent preview(String text) {
        return TagCreator.span(text.length() > CHAR_LIMIT ? text.substring(0, CHAR_LIMIT) + "…" : text)
                .withStyle("color:" + Colors.getHexString(AccentColors.DARK_BLUE));
    }

    private static final int CHAR_LIMIT = 30;
    private static final UnescapedText SEPARATOR = rawHtml("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");

    private static AbstractAction copyToClipboard(String text) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
            }
        };
    }

    private static AbstractAction openFolder(File folder) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    ec.util.desktop.DesktopManager.get().showInFolder(Objects.requireNonNull(folder));
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
        };
    }
}
