package sdmxdl.csv;

import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.DataflowRef;

@lombok.experimental.UtilityClass
public class SdmxCsv {

    public final String DATAFLOW = "DATAFLOW";
    public final String OBS_VALUE = "OBS_VALUE";
    public final String SERIESKEY = "SERIESKEY";

    public @NonNull Formatter<DataflowRef> getDataflowRefFormatter() {
        return SdmxCsv::formatDataflowField;
    }

    public @NonNull Parser<DataflowRef> getDataflowRefParser() {
        return SdmxCsv::parseDataflowField;
    }

    private static String formatDataflowField(DataflowRef ref) {
        return ref != null ? (ref.getAgency() + ":" + ref.getId() + "(" + ref.getVersion() + ")") : null;
    }

    private static DataflowRef parseDataflowField(CharSequence ref) {
        if (ref == null) return null;
        String text = ref.toString();

        int idx1 = text.indexOf(':');
        if (idx1 == -1) return null;

        int idx2 = text.indexOf('(', idx1);
        if (idx2 == -1) return null;

        int idx3 = text.indexOf(')', idx2);
        if (idx3 == -1) return null;

        if (idx3 != text.length() - 1) return null;

        return DataflowRef.of(
                text.substring(0, idx1),
                text.substring(idx1 + 1, idx2),
                text.substring(idx2 + 1, idx3)
        );
    }
}
