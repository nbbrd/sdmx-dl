package tests.sdmxdl.api;

import lombok.NonNull;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import sdmxdl.*;

import static org.assertj.core.api.Assertions.not;
import static org.assertj.core.api.HamcrestCondition.matching;
import static org.assertj.core.condition.NestableCondition.nestable;
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

    public static @NonNull Condition<Dataflow> validDataflow() {
        return Assertions.allOf(
                validResource(),
                validName()
        );
    }

    public static @NonNull Condition<Attribute> validAttribute() {
        return Assertions.allOf(
                validName()
        );
    }

    public static @NonNull Condition<Dimension> validDimension() {
        return Assertions.allOf(
                validName()
        );
    }

    public static <T extends HasName> @NonNull Condition<T> validName() {
        return nestable("name", HasName::getName, not(matching(blankString())));
    }
}
