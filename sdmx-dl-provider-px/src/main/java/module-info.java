import sdmxdl.provider.px.drivers.PxWebDriver;
import sdmxdl.web.spi.Driver;

module sdmxdl.provider.px {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires sdmxdl.api;
    requires sdmxdl.format.xml;
    requires sdmxdl.provider.ri;
    requires sdmxdl.provider.base;
    requires com.google.gson;
    requires nbbrd.io.http;
    requires nbbrd.io.picocsv;

    provides Driver with
            PxWebDriver;
}