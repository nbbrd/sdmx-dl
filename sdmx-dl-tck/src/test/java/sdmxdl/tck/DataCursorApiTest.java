package sdmxdl.tck;

import org.junit.Test;
import sdmxdl.DataCursor;
import sdmxdl.DataFilter;
import sdmxdl.Key;
import sdmxdl.samples.RepoSamples;

import java.util.Arrays;

public class DataCursorApiTest {

    @Test
    public void testEmptyCursor() {
        DataCursorAssert.assertCompliance(
                DataCursor::empty,
                Key.ALL, DataFilter.ALL
        );
    }

    @Test
    public void testSeriesCursor() {
        DataCursorAssert.assertCompliance(
                () -> DataCursor.of(Arrays.asList(RepoSamples.SERIES).iterator()),
                Key.ALL, DataFilter.ALL
        );
    }

    @Test
    public void testFilteredCursor() {
        for (DataFilter.Detail detail : DataFilter.Detail.values()) {
            DataFilter filter = DataFilter.ALL.toBuilder().detail(detail).build();
            DataCursorAssert.assertCompliance(
                    () -> DataCursor.of(Arrays.asList(RepoSamples.SERIES).iterator()).filter(Key.ALL, filter),
                    Key.ALL, filter
            );
        }
    }
}
