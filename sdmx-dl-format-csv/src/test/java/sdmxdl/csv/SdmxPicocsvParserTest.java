package sdmxdl.csv;

import org.junit.jupiter.api.Test;
import sdmxdl.DataSet;
import sdmxdl.util.parser.ObsFactories;
import tests.sdmxdl.api.RepoSamples;

import java.io.IOException;

public class SdmxPicocsvParserTest {

    @Test
    public void test() throws IOException {
        DataSet expected = RepoSamples.DATA_SET;

        String csv = SdmxPicocsvFormatter
                .builder()
                .dsd(RepoSamples.STRUCT)
                .build()
                .formatToString(expected);

        DataSet found = SdmxPicocsvParser
                .builder()
                .dsd(RepoSamples.STRUCT)
                .factory(ObsFactories.SDMX21)
                .build()
                .parseChars(csv);

        System.out.println(csv);
        System.out.println(found);

//        assertThat(found).isEqualTo(expected);
    }
}
