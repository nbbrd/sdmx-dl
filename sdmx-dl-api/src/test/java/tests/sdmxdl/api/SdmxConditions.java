package tests.sdmxdl.api;

import lombok.NonNull;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import sdmxdl.*;

import java.util.Collection;

import static org.assertj.core.api.Assertions.anyOf;
import static org.assertj.core.api.Assertions.not;
import static org.assertj.core.api.HamcrestCondition.matching;
import static org.assertj.core.condition.NestableCondition.nestable;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.text.IsBlankString.blankOrNullString;
import static org.hamcrest.text.IsBlankString.blankString;

@lombok.experimental.UtilityClass
public class SdmxConditions {

    public static <T extends ResourceRef<T>> @NonNull Condition<Resource<T>> validResource() {
        return nestable("ref", Resource::getRef, (Condition<T>) validResourceRef());
    }

    public static <T extends ResourceRef<T>> @NonNull Condition<T> validResourceRef() {
        return nestable("resource",
                nestable("agency", ResourceRef::getAgency, not(matching(blankString()))),
                nestable("id", ResourceRef::getId, not(matching(blankString()))),
                nestable("version", ResourceRef::getVersion, not(matching(blankString())))
        );
    }

    public static @NonNull Condition<Flow> validFlow(boolean allowNoDescription) {
        return Assertions.allOf(
                validResource(),
                validName(),
                validDescription(allowNoDescription)
        );
    }

    public static @NonNull Condition<Structure> validStructure() {
        return Assertions.allOf(
                validResource(),
                validName()
        );
    }

    public static @NonNull Condition<Component> validComponent() {
        return Assertions.allOf(
                validComponentId(),
                validName(),
                nestable("codelist", Component::getCodelist, anyOf(matching(nullValue()), validCodelist()))
        );
    }

    public static @NonNull Condition<Attribute> validAttribute() {
        return Assertions.allOf(
                validComponent()
        );
    }

    public static @NonNull Condition<Dimension> validDimension() {
        return Assertions.allOf(
                validComponent(),
                new Condition<>(Component::isCoded, "a dimension must be coded"),
                //new Condition<>(dimension -> dimension.getPosition() > 0, "a dimension must have a positive position"),
                new Condition<>(dimension -> !dimension.getCodes().isEmpty(), "a dimension must have codes")
        );
    }

    public static <T extends Component> @NonNull Condition<T> validComponentId() {
        return nestable("id", Component::getId, not(matching(blankString())));
    }

    public static <T extends HasName> @NonNull Condition<T> validName() {
        return nestable("name", HasName::getName, not(matching(blankString())));
    }

    public static <T extends HasDescription> @NonNull Condition<T> validDescription(boolean allowNoDescription) {
        return allowNoDescription ? ignore() : nestable("description", HasDescription::getDescription, not(matching(blankOrNullString())));
    }

    public static @NonNull Condition<Codelist> validCodelist() {
        return nestable("codes", Codelist::getCodes, not(matching(blankOrNullString())));
    }

    public static Condition<Collection<? extends Series>> uniqueSeriesKeys() {
        return new Condition<>(o -> o.stream().map(Series::getKey).distinct().count() == o.size(), "unique keys");
    }

    public static Condition<Series> uniqueObs() {
        return new Condition<>(o -> o.getObs().stream().map(Obs::getPeriod).distinct().count() == o.getObs().size(), "unique obs");
    }

    private static <T> Condition<T> ignore() {
        return new Condition<>(ignore -> true, "ignore");
    }
}
