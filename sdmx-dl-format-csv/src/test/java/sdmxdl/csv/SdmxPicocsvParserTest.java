package sdmxdl.csv;

import org.junit.jupiter.api.Test;
import sdmxdl.DataSet;
import sdmxdl.util.parser.DefaultObsParser;
import tests.sdmxdl.api.RepoSamples;

import java.io.IOException;

public class SdmxPicocsvParserTest {

    @Test
    public void test() throws IOException {
        DataSet expected = RepoSamples.DATA_SET;

        String csv = SdmxPicocsvFormatter
                .builder()
                .build()
                .getFormatter(RepoSamples.STRUCT)
                .formatToString(expected);

        DataSet found = SdmxPicocsvParser
                .builder()
                .factory(DefaultObsParser::newDefault)
                .build()
                .getParser(RepoSamples.STRUCT)
                .parseChars(csv);

        System.out.println(csv);
        System.out.println(found);

//        assertThat(found).isEqualTo(expected);
    }
}
