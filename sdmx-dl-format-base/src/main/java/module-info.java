import sdmxdl.ext.Persistence;
import sdmxdl.format.spi.FlowSearchScoringProvider;

module sdmxdl.format.base {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    requires transitive sdmxdl.api;
    requires transitive nbbrd.io.base;

    exports sdmxdl.format;
    exports sdmxdl.format.design;
    exports sdmxdl.format.spi;
    exports sdmxdl.format.time;

    uses Persistence;
    uses FlowSearchScoringProvider;

    provides FlowSearchScoringProvider with
            internal.sdmxdl.format.search.Bm25ScoringProvider,
            internal.sdmxdl.format.search.TrigramScoringProvider;
}