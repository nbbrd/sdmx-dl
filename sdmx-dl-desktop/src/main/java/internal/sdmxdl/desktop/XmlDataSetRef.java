package internal.sdmxdl.desktop;

import nbbrd.io.xml.bind.Jaxb;
import sdmxdl.Key;
import sdmxdl.desktop.DataSetRef;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.io.UncheckedIOException;

public final class XmlDataSetRef {

    private XmlDataSetRef() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static String formatToString(DataSetRef ref) {
        try {
            return Jaxb.Formatter
                    .of(DataSetBean.class)
                    .withFormatted(true)
                    .compose(DataSetBean::from)
                    .formatToString(ref);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @XmlRootElement(name = "dataSetRef")
    @lombok.Data
    public static class DataSetBean {

        XmlDataSourceRef.DataSourceBean dataSourceRef;
        String key;
        int dimensionIndex;

        static DataSetBean from(DataSetRef ref) {
            DataSetBean result = new DataSetBean();
            result.dataSourceRef = XmlDataSourceRef.DataSourceBean.from(ref.getDataSourceRef());
            result.key = ref.getKey().toString();
            result.dimensionIndex = ref.getDimensionIndex();
            return result;
        }

        DataSetRef to() {
            return DataSetRef
                    .builder()
                    .dataSourceRef(dataSourceRef.to())
                    .key(Key.parse(key))
                    .dimensionIndex(dimensionIndex)
                    .build();
        }
    }
}
