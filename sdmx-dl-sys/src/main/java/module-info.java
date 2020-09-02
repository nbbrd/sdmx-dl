module sdmxdl.sys {

    requires static org.checkerframework.checker.qual;
    requires static lombok;

    requires sdmxdl.api;
    requires nbbrd.net.proxy;
    requires nbbrd.picocsv;
    requires com.github.tuupertunut.powershelllibjava;
    requires io.github.hakky54.sslcontext.kickstart;

    exports sdmxdl.sys;
}