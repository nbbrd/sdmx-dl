package sdmxdl.desktop;

import internal.sdmxdl.desktop.util.DynamicTree;
import sdmxdl.*;
import sdmxdl.ext.SdmxCubeUtil;
import sdmxdl.web.SdmxWebManager;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@lombok.AllArgsConstructor
class DataNodeFactory implements DynamicTree.NodeFactory {

    private final Supplier<SdmxWebManager> manager;
    private final Supplier<Languages> languages;

    @Override
    public boolean isLeaf(Object userObject) {
        return userObject instanceof DataSetRef && ((DataSetRef) userObject).getKey().isSeries();
    }

    @Override
    public List<? extends Object> getChildren(Object userObject) throws Exception {
        if (userObject instanceof DataSourceRef) {
            DataSourceRef dataSourceRef = (DataSourceRef) userObject;
            return getChildren(manager.get(), languages.get(), dataSourceRef, Key.ALL);
        } else if (userObject instanceof DataSetRef) {
            DataSetRef dataSetRef = (DataSetRef) userObject;
            return getChildren(manager.get(), languages.get(), dataSetRef.getDataSourceRef(), dataSetRef.getKey());
        }
        return Collections.emptyList();
    }

    private static List<DataSetRef> getChildren(SdmxWebManager manager, Languages languages, DataSourceRef dataSourceRef, Key key) throws IOException {
        try (Connection conn = manager.getConnection(dataSourceRef.getSource(), languages)) {
            Structure dsd = conn.getStructure(dataSourceRef.getFlow());
            List<sdmxdl.Dimension> dimensionList = dsd.getDimensionList();
            Key.Builder builder = Key.builder(dsd);
            for (int i = 0; i < key.size(); i++) {
                builder.put(dimensionList.get(i).getId(), key.get(i));
            }
            int dimensionIndex = getDimensionIndex(dataSourceRef.getDimensions(), dimensionList, getLevel(key));
            return SdmxCubeUtil.getChildren(conn, dataSourceRef.getFlow(), key, dimensionIndex)
                    .sorted()
                    .map(child -> new DataSetRef(dataSourceRef, builder.put(dimensionList.get(dimensionIndex).getId(), child).build(), dimensionIndex))
                    .collect(Collectors.toList());
        }
    }

    private static int getLevel(Key key) {
        return (int) IntStream.range(0, key.size()).filter(i -> !key.isWildcard(i)).count();
    }

    private static int getDimensionIndex(List<String> dimensionIds, List<Dimension> dimensionList, int level) {
        if (dimensionIds.isEmpty()) {
            return level;
        }
        String id = dimensionIds.get(level);
        return IntStream.range(0, dimensionList.size())
                .filter(i -> dimensionList.get(i).getId().equals(id))
                .findFirst()
                .orElse(0);
    }
}
