package sdmxdl.testing;

import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

public enum WebRule implements Function<WebResponse, String> {

    FLOWS_MIN_COUNT {
        @Override
        boolean isInvalid(WebResponse r) {
            return ofNullable(r.getFlows())
                    .map(flows -> flows.size() < r.getRequest().getMinFlowCount())
                    .orElse(false);
        }
    },
    FLOWS_LABEL_NOT_BLANK {
        @Override
        boolean isInvalid(WebResponse r) {
            return ofNullable(r.getFlows())
                    .map(flows -> flows.stream().anyMatch(flow -> isBlank(flow.getLabel())))
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
                    .map(flow -> isBlank(flow.getLabel()))
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
            return dsd != null && flow != null && !dsd.getRef().contains(flow.getStructureRef());
        }
    },
    STRUCT_DIMENSION_COUNT {
        @Override
        boolean isInvalid(WebResponse r) {
            return ofNullable(r.getStructure())
                    .map(dsd -> dsd.getDimensions().size() != r.getRequest().getDimensionCount())
                    .orElse(false);
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
    DATA_MIN_SERIES_COUNT {
        @Override
        boolean isInvalid(WebResponse r) {
            return ofNullable(r.getData())
                    .map(data -> data.size() < r.getRequest().getMinSeriesCount())
                    .orElse(false);
        }
    },
    DATA_MIN_OBS_COUNT {
        @Override
        boolean isInvalid(WebResponse r) {
            return ofNullable(r.getData())
                    .map(data -> getObsCount(data) < r.getRequest().getMinObsCount())
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
    FLOW_MISSING {
        @Override
        boolean isInvalid(WebResponse r) {
            return r.getFlow() == null;
        }
    },
    STRUCT_MISSING {
        @Override
        boolean isInvalid(WebResponse r) {
            return r.getStructure() == null;
        }
    },
    DATA_MISSING {
        @Override
        boolean isInvalid(WebResponse r) {
            return r.getData() == null;
        }
    },
    STRUCT_UNCODED_DIMENSION {
        @Override
        boolean isInvalid(WebResponse r) {
            return ofNullable(r.getStructure())
                    .map(dsd -> dsd.getDimensions().stream().anyMatch(dimension -> !dimension.isCoded()))
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

    @Override
    final public String apply(WebResponse response) {
        return isInvalid(response) ? name() : null;
    }

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

    public static int getObsCount(Collection<Series> data) {
        return data.stream().mapToInt(series -> series.getObs().size()).sum();
    }
}
