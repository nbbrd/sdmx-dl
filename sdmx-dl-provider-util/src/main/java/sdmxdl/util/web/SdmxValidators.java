package sdmxdl.util.web;

import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.DataStructure;
import sdmxdl.DataflowRef;
import sdmxdl.Key;
import sdmxdl.web.SdmxWebSource;

import java.util.Objects;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static sdmxdl.util.SdmxPatterns.*;
import static sdmxdl.util.web.Validator.onRegex;

@lombok.experimental.UtilityClass
public class SdmxValidators {

    public static final Validator<DataflowRef> DEFAULT_DATAFLOW_REF_VALIDATOR = dataflowRefOf(
            AGENCY_ID_PATTERN,
            RESOURCE_ID_PATTERN,
            VERSION_PATTERN
    );

    public static @NonNull Validator<DataflowRef> dataflowRefOf(@NonNull Pattern agency, @NonNull Pattern id, @NonNull Pattern version) {
        return Validator.onAll(asList(
                onRegex("DataflowRef agency", agency).compose(DataflowRef::getAgency),
                onRegex("DataflowRef id", id).compose(DataflowRef::getId),
                onRegex("DataflowRef version", version).compose(DataflowRef::getVersion)
        ));
    }

    public static @NonNull Validator<SdmxWebSource> onDriverName(@NonNull String driverName) {
        Objects.requireNonNull(driverName);
        return source -> source == null || !source.getDriver().equals(driverName)
                ? String.format("Expected driver name '%s' to be '%s'", source.getDriver(), driverName)
                : null;
    }

    public static @NonNull Validator<Key> onDataStructure(@NonNull DataStructure dsd) {
        Objects.requireNonNull(dsd);
        return key -> key != null ? key.validateOn(dsd) : "Missing key";
    }
}
