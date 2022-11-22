package sdmxdl.testing;

import nbbrd.service.ServiceProvider;
import sdmxdl.*;
import sdmxdl.provider.Validator;
import sdmxdl.provider.web.WebValidators;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

public enum OtherRules {

    FLOWS_LABEL_NOT_BLANK {
        @Override
        boolean isInvalid(WebResponse r) {
            return ofNullable(r.getFlows())
                    .map(flows -> flows.stream().anyMatch(flow -> isBlank(flow.getName())))
                    .orElse(false);
        }
    },
    FLOWS_DATAFLOW_REF_PATTERN {
        @Override
        boolean isInvalid(WebResponse r) {
            return ofNullable(r.getFlows())
                    .map(flows -> flows.stream().anyMatch(flow -> WebValidators.DEFAULT_DATAFLOW_REF_VALIDATOR.validate(flow.getRef()) != null))
                    .orElse(false);
        }
    },
    FLOWS_NO_DESCRIPTION {
        @Override
        boolean isInvalid(WebResponse r) {
            return ofNullable(r.getFlows())
                    .map(flows -> flows.stream().allMatch(flow -> flow.getDescription().isEmpty()))
                    .orElse(false);
        }
    },
    FLOW_INVALID_REF {
        @Override
        boolean isInvalid(WebResponse r) {
            return ofNullable(r.getFlow())
                    .map(flow -> !r.getRequest().getFlowRef().containsRef(flow))
                    .orElse(false);
        }
    },
    FLOW_LABEL_NOT_BLANK {
        @Override
        boolean isInvalid(WebResponse r) {
            return ofNullable(r.getFlow())
                    .map(flow -> isBlank(flow.getName()))
                    .orElse(false);
        }
    },
    STRUCT_LABEL_NOT_BLANK {
        @Override
        boolean isInvalid(WebResponse r) {
            return ofNullable(r.getStructure())
                    .map(dsd -> isBlank(dsd.getLabel()))
                    .orElse(false);
        }
    },
    STRUCT_INVALID_REF {
        @Override
        boolean isInvalid(WebResponse r) {
            DataStructure dsd = r.getStructure();
            Dataflow flow = r.getFlow();
            return dsd != null && flow != null && !flow.getStructureRef().contains(dsd.getRef());
        }
    },
    STRUCT_INVALID_DIMENSION {
        @Override
        boolean isInvalid(WebResponse r) {
            return ofNullable(r.getStructure())
                    .map(dsd -> dsd
                            .getDimensions()
                            .stream()
                            .map(dimension -> checkDimension(name(), dimension))
                            .anyMatch(Objects::nonNull))
                    .orElse(false);
        }
    },
    DATA_META_NOT_BLANK {
        @Override
        boolean isInvalid(WebResponse r) {
            return ofNullable(r.getData())
                    .map(data -> data.stream()
                            .map(Series::getMeta)
                            .map(meta -> checkMap(name(), meta))
                            .anyMatch(Objects::nonNull))
                    .orElse(false);
        }
    },
    STRUCT_NO_TIME_UNIT {
        @Override
        boolean isInvalid(WebResponse r) {
            return ofNullable(r.getStructure())
                    .map(dsd -> !hasTimeFormatAttribute(dsd) && !hasFreqDimension(dsd))
                    .orElse(false);
        }

        private boolean hasTimeFormatAttribute(DataStructure dsd) {
            return dsd.getAttributes().stream().map(Attribute::getId).anyMatch("TIME_FORMAT"::equals);
        }

        private boolean hasFreqDimension(DataStructure dsd) {
            return dsd.getDimensions().stream().map(Dimension::getId).anyMatch("FREQ"::equals);
        }
    },
    MISSING_FLOW {
        @Override
        boolean isInvalid(WebResponse r) {
            return !Validator.onNotNull("flow").compose(WebResponse::getFlow).isValid(r);
        }
    },
    MISSING_STRUCT {
        @Override
        boolean isInvalid(WebResponse r) {
            return !Validator.onNotNull("structure").compose(WebResponse::getStructure).isValid(r);
        }
    },
    MISSING_DATA {
        @Override
        boolean isInvalid(WebResponse r) {
            return !Validator.onNotNull("data").compose(WebResponse::getData).isValid(r);
        }
    },
    STRUCT_NOT_CODED_DIMENSION {
        @Override
        boolean isInvalid(WebResponse r) {
            return ofNullable(r.getStructure())
                    .map(dsd -> dsd.getDimensions().stream().anyMatch(dimension -> !dimension.isCoded()))
                    .orElse(false);
        }
    },
    STRUCT_MISSING_CODES_DIMENSION {
        @Override
        boolean isInvalid(WebResponse r) {
            return ofNullable(r.getStructure())
                    .map(dsd -> dsd.getDimensions().stream().anyMatch(dimension -> dimension.getCodes().isEmpty()))
                    .orElse(false);
        }
    },
    DATA_NULL_PERIOD {
        @Override
        boolean isInvalid(WebResponse r) {
            return ofNullable(r.getData())
                    .map(data -> data.stream()
                            .flatMap(series -> series.getObs().stream())
                            .anyMatch(obs -> obs.getPeriod() == null))
                    .orElse(false);
        }
    };

    abstract boolean isInvalid(WebResponse r);

    private static boolean isBlank(String o) {
        return o.trim().isEmpty();
    }

    private static String checkDimension(String checkName, Dimension dimension) {
        return isBlank(dimension.getId())
                || isBlank(dimension.getLabel())
                || dimension.getPosition() <= 0
                || dimension.getCodes().isEmpty()
                ? checkName
                : checkMap(checkName, dimension.getCodes());
    }

    private static String checkMap(String checkName, Map<String, String> map) {
        return map.entrySet()
                .stream()
                .map(entry -> checkEntry(checkName, entry))
                .filter(Objects::nonNull)
                .findAny()
                .orElse(null);
    }

    private static String checkEntry(String checkName, Map.Entry<String, String> entry) {
        return isBlank(entry.getKey()) || isBlank(entry.getValue())
                ? checkName
                : null;
    }

    @ServiceProvider
    public static final class OtherRulesProvider implements WebRuleBatch {

        @Override
        public Stream<WebRule> getProviders() {
            return Stream.of(OtherRules.values()).map(item -> WebRule.of(item.name(), response -> item.isInvalid(response) ? item.name() : null));
        }
    }
}
