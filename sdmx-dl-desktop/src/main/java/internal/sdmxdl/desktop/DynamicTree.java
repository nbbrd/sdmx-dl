package internal.sdmxdl.desktop;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DynamicTree {

    public static void enable(JTree tree, NodeFactory nodeFactory, Object root) {
        tree.addTreeWillExpandListener(new TreeWillExpandListener() {

            @Override
            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
                if (!nodeFactory.isLeaf(node) && !node.children().hasMoreElements()) {
                    SwingWorker<List<?>, Void> worker = new SwingWorker<List<?>, Void>() {
                        @Override
                        protected List<?> doInBackground() throws Exception {
                            return nodeFactory.getChildren(node.getUserObject());
                        }

                        @Override
                        protected void done() {
                            node.removeAllChildren();
                            try {
                                get().forEach(o -> node.add(new CustomNode(o, nodeFactory.isLeaf(o))));
                            } catch (InterruptedException | ExecutionException ex) {
                                node.add(new DefaultMutableTreeNode(ex));
                            }
                            ((DefaultTreeModel) tree.getModel()).nodeStructureChanged(node);
                        }
                    };
                    node.add(new DefaultMutableTreeNode(worker));
                    worker.execute();
                }
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
            }
        });
        tree.setModel(new DefaultTreeModel(new CustomNode(root, nodeFactory.isLeaf(root))));
    }

    public static class CustomNode extends DefaultMutableTreeNode {

        private final boolean leaf;

        public CustomNode(Object userObject, boolean leaf) {
            super(userObject);
            this.leaf = leaf;
        }

        @Override
        public boolean isLeaf() {
            return leaf;
        }
    }

    public interface NodeFactory {

        boolean isLeaf(Object userObject);

        List<? extends Object> getChildren(Object userObject) throws Exception;
    }
}
