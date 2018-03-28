/*
 * Copyright 2018 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package internal.util.drivers;

import be.nbb.sdmx.facade.web.SdmxWebSource;
import ioutil.Jaxb;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public final class SdmxWebResource {

    public static List<SdmxWebSource> load(String resource) {
        try {
            return Jaxb.Parser.of(XSources.class)
                    .parseStream(() -> SdmxWebResource.class.getResourceAsStream(resource))
                    .toSources();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @XmlRootElement(name = "sources")
    public static final class XSources {

        public XSource[] source;

        public List<SdmxWebSource> toSources() {
            return source != null
                    ? Stream.of(source).map(XSource::toSource).collect(Collectors.toList())
                    : Collections.emptyList();
        }

        public static XSources of(List<SdmxWebSource> o) {
            XSources result = new XSources();
            result.source = o.stream().map(XSource::of).toArray(XSource[]::new);
            return result;
        }
    }

    public static final class XSource {

        public String name;

        public String description;

        public String driver;

        public String endpoint;

        public XProperty[] property;

        public SdmxWebSource toSource() {
            SdmxWebSource.Builder result = SdmxWebSource
                    .builder()
                    .name(name)
                    .description(description)
                    .driver(driver)
                    .endpointOf(endpoint);
            if (property != null) {
                for (XProperty o : property) {
                    result.property(o.key, o.value);
                }
            }
            return result.build();
        }

        public static XSource of(SdmxWebSource o) {
            XSource result = new XSource();
            result.name = o.getName();
            result.description = o.getDescription();
            result.driver = o.getDriver();
            result.endpoint = o.getEndpoint().toString();
            result.property = o.getProperties().entrySet().stream().map(XProperty::of).toArray(XProperty[]::new);
            return result;
        }
    }

    public static final class XProperty {

        @XmlAttribute
        String key;

        @XmlAttribute
        String value;

        public static XProperty of(Map.Entry<String, String> o) {
            XProperty result = new XProperty();
            result.key = o.getKey();
            result.value = o.getValue();
            return result;
        }
    }
}
