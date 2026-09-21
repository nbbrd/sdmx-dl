package tests.sdmxdl.api;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.HamcrestCondition.matching;
import static org.assertj.core.condition.NestableCondition.nestable;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.text.IsBlankString.blankOrNullString;
import static org.hamcrest.text.IsBlankString.blankString;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.NonNull;
import org.assertj.core.api.Condition;
import sdmxdl.*;

@lombok.experimental.UtilityClass
public class SdmxConditions {

    public static <T extends ResourceRef<T>> @NonNull Condition<Resource<T>> validResource() {
        return nestable("ref", Resource::getRef, (Condition<T>) validResourceRef());
    }

    public static <T extends ResourceRef<T>> @NonNull Condition<T> validResourceRef() {
        return nestable(
                "resource",
                nestable("agency", ResourceRef::getAgency, not(matching(blankString()))),
                nestable("id", ResourceRef::getId, not(matching(blankString()))),
                nestable("version", ResourceRef::getVersion, not(matching(blankString()))));
    }

    public static @NonNull Condition<Flow> validFlow(boolean allowNoDescription) {
        return allOf(validResource(), validName(), validDescription(allowNoDescription));
    }

    public static @NonNull Condition<Structure> validStructure() {
        return allOf(validResource(), validName());
    }

    public static @NonNull Condition<Component> validComponent() {
        return allOf(
                validComponentId(),
                validName(),
                nestable("codelist", Component::getCodelist, anyOf(matching(nullValue()), validCodelist())));
    }

    public static @NonNull Condition<Attribute> validAttribute() {
        return allOf(validComponent());
    }

    public static @NonNull Condition<Dimension> validDimension() {
        return allOf(validComponent(), nonNegativeIndex());
    }

    public static @NonNull Condition<Collection<? extends Dimension>> orderedByDimensionIndex() {
        return new Condition<>(
                SdmxConditions::isOrderedByDimensionIndex,
                "a dimension list must be a 0-based sequence incremented by 1");
    }

    private static Condition<Dimension> nonNegativeIndex() {
        return new Condition<>(dimension -> dimension.getIndex() >= 0, "a dimension must have a non-negative index");
    }

    public static <T extends Component> @NonNull Condition<T> validComponentId() {
        return nestable("id", Component::getId, not(matching(blankString())));
    }

    public static <T extends HasName> @NonNull Condition<T> validName() {
        return nestable("name", HasName::getName, not(matching(blankString())));
    }

    public static <T extends HasDescription> @NonNull Condition<T> validDescription(boolean allowNoDescription) {
        return allowNoDescription
                ? ignore()
                : nestable("description", HasDescription::getDescription, not(matching(blankOrNullString())));
    }

    public static @NonNull Condition<Codelist> validCodelist() {
        return nestable("codes", Codelist::getCodes, not(matching(blankOrNullString())));
    }

    public static Condition<Collection<? extends Series>> uniqueSeriesKeys() {
        return new Condition<>(o -> o.stream().map(Series::getKey).distinct().count() == o.size(), "unique keys");
    }

    public static Condition<Series> uniqueObs() {
        return new Condition<>(
                series ->
                        series.getObs().stream().map(Obs::getPeriod).distinct().count()
                                == series.getObs().size(),
                "unique obs");
    }

    private static <T> Condition<T> ignore() {
        return new Condition<>(ignore -> true, "ignore");
    }

    private static boolean isOrderedByDimensionIndex(Collection<? extends Dimension> list) {
        AtomicInteger expectedIndex = new AtomicInteger(0);
        return list.stream().mapToInt(Dimension::getIndex).allMatch(index -> index == expectedIndex.getAndIncrement());
    }
}
