import sdmxdl.provider.dialects.drivers.*;
import sdmxdl.web.spi.Driver;

module sdmxdl.provider.dialects {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    requires sdmxdl.api;
    requires sdmxdl.format.xml;
    requires sdmxdl.provider.ri;
    requires sdmxdl.provider.base;
    requires com.google.gson;
    requires nbbrd.io.http;

    provides Driver with
            BbkDialectDriver,
            DotStatDialectDriver,
            EstatDialectDriver,
            ImfDialectDriver,
            InseeDialectDriver,
            StatCanDialectDriver;
}