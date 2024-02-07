package sdmxdl.testing;

import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import sdmxdl.Flow;
import sdmxdl.Series;
import sdmxdl.Structure;
import sdmxdl.provider.Validator;

import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

public enum RangeRules {

    FLOWS_COUNT {
        @Override
        String validateRange(WebResponse r) {
            return getValidator(r.getRequest()).validate(r.getFlows());
        }

        private Validator<Collection<Flow>> getValidator(WebRequest request) {
            return RangeRules
                    .onRange(request.getFlowCount())
                    .compose((Collection<Flow> o) -> o.size())
                    .onlyIf(Objects::nonNull);
        }
    },
    STRUCT_DIMENSION_COUNT {
        @Override
        String validateRange(WebResponse r) {
            return getValidator(r.getRequest()).validate(r.getStructure());
        }

        private Validator<Structure> getValidator(WebRequest request) {
            return RangeRules
                    .onRange(request.getDimensionCount())
                    .compose((Structure o) -> o.getDimensions().size())
                    .onlyIf(Objects::nonNull);
        }
    },
    DATA_SERIES_COUNT {
        @Override
        String validateRange(WebResponse r) {
            return getValidator(r.getRequest()).validate(r.getData());
        }

        private Validator<Collection<Series>> getValidator(WebRequest request) {
            return RangeRules
                    .onRange(request.getSeriesCount())
                    .compose((Collection<Series> o) -> o.size())
                    .onlyIf(Objects::nonNull);
        }
    },
    DATA_OBS_COUNT {
        @Override
        String validateRange(WebResponse r) {
            return getValidator(r.getRequest()).validate(r.getData());
        }

        private Validator<Collection<Series>> getValidator(WebRequest request) {
            return RangeRules
                    .onRange(request.getObsCount())
                    .compose(WebResponse::getObsCount)
                    .onlyIf(Objects::nonNull);
        }
    };

    abstract String validateRange(WebResponse r);

    private static Validator<Integer> onRange(IntRange range) {
        return value -> !range.contains(value) ? String.format(Locale.ROOT, "Expecting range '%s' to contain value %s", range.toShortString(), value) : null;
    }

    @DirectImpl
    @ServiceProvider
    public static final class RangeRulesProvider implements WebRuleBatch {

        @Override
        public Stream<WebRule> getProviders() {
            return Stream.of(RangeRules.values())
                    .map(item -> WebRuleSupport
                            .builder()
                            .id(item.name())
                            .validator(item::validateRange)
                            .build()
                    );
        }
    }
}
