package internal.sdmxdl.desktop.util;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public final class JTrees {

    private JTrees() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static <T> Collector<T, ?, DefaultMutableTreeNode> toDefaultMutableTreeNode(Object userObject) {
        return Collectors.collectingAndThen(toList(), list -> {
            DefaultMutableTreeNode result = new DefaultMutableTreeNode(userObject);
            list.forEach(item -> result.add(new DefaultMutableTreeNode(item)));
            return result;
        });
    }

    public static Stream<TreeNode> childStreamOf(TreeNode node) {
        return IntStream.range(0, node.getChildCount()).mapToObj(node::getChildAt);
    }

    public static void expandOrCollapseAll(JTree tree, boolean expand) {
        expandOrCollapseAll(tree, ignore -> expand);
    }

    public static void expandOrCollapseAll(JTree tree, Predicate<? super TreePath> predicate) {
        TreeNode root = (TreeNode) tree.getModel().getRoot();
        TreePath parent = new TreePath(root);
        if (tree.isRootVisible()) {
            expandOrCollapseAll(tree, parent, predicate);
        } else {
            childStreamOf(root)
                    .map(parent::pathByAddingChild)
                    .forEach(path -> expandOrCollapseAll(tree, path, predicate));
        }
    }

    private static void expandOrCollapseAll(JTree tree, TreePath parent, Predicate<? super TreePath> predicate) {
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        childStreamOf(node)
                .map(parent::pathByAddingChild)
                .forEach(path -> expandOrCollapseAll(tree, path, predicate));
        if (predicate.test(parent)) tree.expandPath(parent);
        else tree.collapsePath(parent);
    }

    public static <T> TreeCellRenderer cellRendererOf(Class<T> type, BiConsumer<? super JLabel, ? super T> consumer) {
        return new FunctionalRenderer<>(type, consumer);
    }

    private static final class FunctionalRenderer<T> extends DefaultTreeCellRenderer {

        private final Class<T> type;
        private final BiConsumer<? super JLabel, ? super T> consumer;

        public FunctionalRenderer(Class<T> type, BiConsumer<? super JLabel, ? super T> renderer) {
            this.type = type;
            this.consumer = renderer;
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            JLabel renderer = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            if (value instanceof DefaultMutableTreeNode) {
                Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
                if (type.isInstance(userObject)) {
                    consumer.accept(renderer, type.cast(userObject));
                }
            }
            return renderer;
        }
    }
}
