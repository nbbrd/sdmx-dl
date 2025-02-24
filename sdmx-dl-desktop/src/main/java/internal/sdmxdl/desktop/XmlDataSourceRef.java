package internal.sdmxdl.desktop;

import nbbrd.io.xml.Xml;
import nbbrd.io.xml.bind.Jaxb;
import sdmxdl.DatabaseRef;
import sdmxdl.Languages;
import sdmxdl.desktop.DataSourceRef;
import sdmxdl.desktop.Toggle;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

public final class XmlDataSourceRef {

    private XmlDataSourceRef() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final Xml.Parser<List<DataSourceRef>> LIST_PARSER = Jaxb.Parser.of(DataSourceBeans.class).andThen(DataSourceBeans::to);
    public static final Xml.Formatter<List<DataSourceRef>> LIST_FORMATTER = Jaxb.Formatter.of(DataSourceBeans.class).compose(DataSourceBeans::from);

    public static String formatToString(DataSourceRef ref) {
        try {
            return Jaxb.Formatter
                    .of(DataSourceBean.class)
                    .withFormatted(true)
                    .compose(DataSourceBean::from)
                    .formatToString(ref);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @XmlRootElement(name = "dataSourceRefs")
    @lombok.Data
    public static class DataSourceBeans {

        List<DataSourceBean> list;

        static DataSourceBeans from(List<DataSourceRef> list) {
            DataSourceBeans result = new DataSourceBeans();
            result.list = list.stream().map(DataSourceBean::from).collect(Collectors.toList());
            return result;
        }

        List<DataSourceRef> to() {
            return list != null ? list.stream().map(DataSourceBean::to).collect(Collectors.toList()) : emptyList();
        }
    }


    @XmlRootElement(name = "dataSourceRef")
    @lombok.Data
    public static class DataSourceBean {

        String source;
        String database;
        String flow;
        List<String> dimensions;
        String languages;
        boolean debug;
        Toggle curlBackend;

        static DataSourceBean from(DataSourceRef ref) {
            DataSourceBean result = new DataSourceBean();
            result.source = ref.getSource();
            result.database = ref.getDatabase().toString();
            result.flow = ref.getFlow();
            result.dimensions = ref.getDimensions();
            result.languages = ref.getLanguages().toString();
            result.debug = ref.isDebug();
            result.curlBackend = ref.getCurlBackend();
            return result;
        }

        DataSourceRef to() {
            return DataSourceRef
                    .builder()
                    .source(source)
                    .database(DatabaseRef.parse(database))
                    .flow(flow)
                    .dimensions(dimensions != null ? dimensions : emptyList())
                    .languages(Languages.parse(languages))
                    .debug(debug)
                    .curlBackend(curlBackend)
                    .build();
        }
    }
}
