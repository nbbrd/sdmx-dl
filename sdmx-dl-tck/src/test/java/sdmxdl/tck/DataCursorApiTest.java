package sdmxdl.tck;

import org.junit.jupiter.api.Test;
import sdmxdl.DataCursor;
import sdmxdl.DataFilter;
import sdmxdl.Key;
import sdmxdl.samples.RepoSamples;

import java.util.Arrays;

import static sdmxdl.tck.DataFilterAssert.filters;

public class DataCursorApiTest {

    @Test
    public void testEmptyCursor() {
        DataCursorAssert.assertCompliance(
                DataCursor::empty,
                Key.ALL, DataFilter.FULL
        );
    }

    @Test
    public void testSeriesCursor() {
        DataCursorAssert.assertCompliance(
                () -> DataCursor.of(Arrays.asList(RepoSamples.S1).iterator()),
                Key.ALL, DataFilter.FULL
        );
    }

    @Test
    public void testFilteredCursor() {
        for (DataFilter filter : filters(DataFilter.Detail.values())) {
            DataCursorAssert.assertCompliance(
                    () -> DataCursor.of(Arrays.asList(RepoSamples.S1).iterator()).filter(Key.ALL, filter),
                    Key.ALL, filter
            );
        }
    }
}
