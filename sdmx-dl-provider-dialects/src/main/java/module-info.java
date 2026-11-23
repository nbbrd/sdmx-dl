import sdmxdl.provider.dialects.drivers.*;
import sdmxdl.provider.dialects.icons.DialectsIconProvider;
import sdmxdl.web.spi.Driver;
import sdmxdl.web.spi.IconProvider;

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
            IneDialectDriver,
            InseeDialectDriver,
            StatCanDialectDriver,
            UisDialectDriver;
    provides IconProvider with
            DialectsIconProvider;
}
