package sdmxdl.provider.web;

import lombok.NonNull;
import sdmxdl.DataStructure;
import sdmxdl.DataflowRef;
import sdmxdl.Key;
import sdmxdl.provider.SdmxPatterns;
import sdmxdl.provider.Validator;
import sdmxdl.web.SdmxWebSource;

import java.util.Locale;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;

@lombok.experimental.UtilityClass
public class WebValidators {

    public static final Validator<DataflowRef> DEFAULT_DATAFLOW_REF_VALIDATOR = dataflowRefOf(
            SdmxPatterns.AGENCY_ID_PATTERN,
            SdmxPatterns.RESOURCE_ID_PATTERN,
            SdmxPatterns.VERSION_PATTERN
    );

    public static @NonNull Validator<DataflowRef> dataflowRefOf(@NonNull Pattern agency, @NonNull Pattern id, @NonNull Pattern version) {
        return Validator.onAll(asList(
                Validator.onRegex("DataflowRef agency", agency).compose(DataflowRef::getAgency),
                Validator.onRegex("DataflowRef id", id).compose(DataflowRef::getId),
                Validator.onRegex("DataflowRef version", version).compose(DataflowRef::getVersion)
        ));
    }

    public static @NonNull Validator<SdmxWebSource> onDriverId(@NonNull String driverId) {
        return source -> source != null && !source.getDriver().equals(driverId)
                ? String.format(Locale.ROOT, "Expecting driver name '%s' to be '%s'", source.getDriver(), driverId)
                : null;
    }

    public static @NonNull Validator<Key> onDataStructure(@NonNull DataStructure dsd) {
        return key -> key != null ? key.validateOn(dsd) : "Missing key";
    }
}
