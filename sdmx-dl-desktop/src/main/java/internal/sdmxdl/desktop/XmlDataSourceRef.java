package internal.sdmxdl.desktop;

import nbbrd.io.xml.Xml;
import nbbrd.io.xml.bind.Jaxb;
import sdmxdl.Languages;
import sdmxdl.desktop.DataSourceRef;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

public final class XmlDataSourceRef {

    private XmlDataSourceRef() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final Xml.Parser<List<DataSourceRef>> PARSER = Jaxb.Parser.of(DataSourceBeans.class).andThen(DataSourceBeans::to);
    public static final Xml.Formatter<List<DataSourceRef>> FORMATTER = Jaxb.Formatter.of(DataSourceBeans.class).compose(DataSourceBeans::from);

    @XmlRootElement
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


    @XmlRootElement
    @lombok.Data
    public static class DataSourceBean {

        String source;
        String catalog;
        String flow;
        List<String> dimensions;
        String languages;

        static DataSourceBean from(DataSourceRef ref) {
            DataSourceBean result = new DataSourceBean();
            result.source = ref.getSource();
            result.catalog = ref.getCatalog();
            result.flow = ref.getFlow();
            result.dimensions = ref.getDimensions();
            result.languages = ref.getLanguages().toString();
            return result;
        }

        DataSourceRef to() {
            return DataSourceRef
                    .builder()
                    .source(source)
                    .catalog(catalog)
                    .flow(flow)
                    .dimensions(dimensions != null ? dimensions : emptyList())
                    .languages(Languages.parse(languages))
                    .build();
        }
    }
}
