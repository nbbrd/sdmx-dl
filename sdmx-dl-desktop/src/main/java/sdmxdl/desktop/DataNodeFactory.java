package sdmxdl.desktop;

import internal.sdmxdl.desktop.util.DynamicTree;
import sdmxdl.Connection;
import sdmxdl.Dimension;
import sdmxdl.Key;
import sdmxdl.Structure;
import sdmxdl.web.SdmxWebManager;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

@lombok.AllArgsConstructor
class DataNodeFactory implements DynamicTree.NodeFactory {

    private final Supplier<SdmxWebManager> manager;

    @Override
    public boolean isLeaf(Object userObject) {
        return userObject instanceof DataSetRef && ((DataSetRef) userObject).getKey().isSeries();
    }

    @Override
    public List<? extends Object> getChildren(Object userObject) throws Exception {
        if (userObject instanceof DataSourceRef) {
            DataSourceRef dataSourceRef = (DataSourceRef) userObject;
            return getChildren(manager.get(), dataSourceRef, Key.ALL);
        } else if (userObject instanceof DataSetRef) {
            DataSetRef dataSetRef = (DataSetRef) userObject;
            return getChildren(manager.get(), dataSetRef.getDataSourceRef(), dataSetRef.getKey());
        }
        return Collections.emptyList();
    }

    private static List<DataSetRef> getChildren(SdmxWebManager manager, DataSourceRef dataSourceRef, Key key) throws IOException {
        try (Connection conn = dataSourceRef.getConnection(manager)) {
            Structure dsd = conn.getMeta(dataSourceRef.getDatabase(), dataSourceRef.toFlowRef()).getStructure();
            Key base = key.expand(dsd);
            int dimensionIndex = getDimensionIndex(dataSourceRef.getDimensions(), dsd.getDimensionList(), getLevel(key));
            return conn.getAvailableDimensionCodes(dataSourceRef.getDatabase(), dataSourceRef.toFlowRef(), key, dimensionIndex)
                    .stream()
                    .sorted()
                    .map(child -> new DataSetRef(dataSourceRef, base.with(child, dimensionIndex), dimensionIndex))
                    .collect(toList());
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
