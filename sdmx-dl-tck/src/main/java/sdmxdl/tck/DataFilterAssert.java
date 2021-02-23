package sdmxdl.tck;

import org.assertj.core.api.AbstractAssert;
import sdmxdl.DataFilter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataFilterAssert extends AbstractAssert<DataFilterAssert, DataFilter> {

    public DataFilterAssert(DataFilter actual) {
        super(actual, DataFilterAssert.class);
    }

    public static List<DataFilter> filters(DataFilter.Detail... details) {
        return Stream.of(details).map(o -> DataFilter.builder().detail(o).build()).collect(Collectors.toList());
    }
}
