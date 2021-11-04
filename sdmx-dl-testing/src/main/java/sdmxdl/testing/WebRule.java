package sdmxdl.testing;

import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.Dimension;
import sdmxdl.Series;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum WebRule implements Function<WebResponse, String> {

    FLOWS_MIN_COUNT {
        @Override
        public String apply(WebResponse r) {
            return r.hasFlows() && r.getFlows().size() < r.getRequest().getMinFlowCount()
                    ? name()
                    : null;
        }
    },
    FLOWS_LABEL_NOT_BLANK {
        @Override
        public String apply(WebResponse r) {
            return r.hasFlows()
                    ? r.getFlows()
                    .stream()
                    .filter(flow -> isBlank(flow.getLabel()))
                    .findAny()
                    .map(flow -> name())
                    .orElse(null)
                    : null;
        }
    },
    FLOW_INVALID_REF {
        @Override
        public String apply(WebResponse r) {
            return r.hasFlow() && !r.getRequest().getDataRef().getFlowRef().containsRef(r.getFlow())
                    ? name()
                    : null;
        }
    },
    FLOW_LABEL_NOT_BLANK {
        @Override
        public String apply(WebResponse r) {
            return r.hasFlow() && isBlank(r.getFlow().getLabel())
                    ? name()
                    : null;
        }
    },
    STRUCT_LABEL_NOT_BLANK {
        @Override
        public String apply(WebResponse r) {
            return r.hasStructure() && isBlank(r.getStructure().getLabel())
                    ? name()
                    : null;
        }
    },
    STRUCT_INVALID_REF {
        @Override
        public String apply(WebResponse r) {
            return r.hasStructure() && r.hasFlow() && !r.getStructure().getRef().contains(r.getFlow().getStructureRef())
                    ? name()
                    : null;
        }
    },
    STRUCT_DIMENSION_COUNT {
        @Override
        public String apply(WebResponse r) {
            return r.hasStructure() && r.getStructure().getDimensions().size() != r.getRequest().getDimensionCount()
                    ? name()
                    : null;
        }
    },
    STRUCT_INVALID_DIMENSION {
        @Override
        public String apply(WebResponse r) {
            return r.hasStructure()
                    ? r.getStructure()
                    .getDimensions()
                    .stream()
                    .map(dimension -> checkDimension(name(), dimension))
                    .filter(Objects::nonNull)
                    .findAny()
                    .orElse(null)
                    : null;
        }
    },
    DATA_MIN_SERIES_COUNT {
        @Override
        public String apply(WebResponse r) {
            return r.hasData() && r.getData().size() < r.getRequest().getMinSeriesCount()
                    ? name()
                    : null;
        }
    },
    DATA_MIN_OBS_COUNT {
        @Override
        public String apply(WebResponse r) {
            return r.hasData() && r.getData()
                    .stream()
                    .map(Series::getObs)
                    .mapToInt(Collection::size)
                    .sum() < r.getRequest().getMinObsCount()
                    ? name()
                    : null;
        }
    },
    DATA_META_NOT_BLANK {
        @Override
        public String apply(WebResponse r) {
            return r.hasData()
                    ? r.getData()
                    .stream()
                    .map(Series::getMeta)
                    .map(meta -> checkMap(name(), meta))
                    .filter(Objects::nonNull)
                    .findAny()
                    .orElse(null)
                    : null;
        }
    },
    NO_TIME_UNIT {
        @Override
        public String apply(WebResponse r) {
            if (!r.hasStructure())
                return null;
            boolean timeFormat = r.getStructure().getAttributes().stream()
                    .anyMatch(attribute -> attribute.getId().equals("TIME_FORMAT"));
            boolean freq = r.getStructure().getDimensions().stream()
                    .anyMatch(attribute -> attribute.getId().equals("FREQ"));
            return timeFormat || freq ? null : name();
        }
    },
    NO_FLOW {
        @Override
        public String apply(WebResponse webResponse) {
            return webResponse.hasFlow() ? null : name();
        }
    },
    NO_STRUCT {
        @Override
        public String apply(WebResponse webResponse) {
            return webResponse.hasStructure() ? null : name();
        }
    },
    DIMENSION_NOT_CODED {
        @Override
        public String apply(WebResponse r) {
            return r.hasStructure() && r.getStructure().getDimensions().stream().anyMatch(dimension -> !dimension.isCoded())
                    ? name() : null;
        }
    };

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

    @NonNull
    public static List<String> checkAll(@NonNull WebResponse response) {
        return Stream.of(WebRule.values())
                .map(check -> check.apply(response))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
