/*
 * Copyright 2020 National Bank of Belgium
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
package internal.sdmxdl.cli;

import nbbrd.console.picocli.yaml.YamlOutput;
import nbbrd.console.picocli.yaml.YamlOutputOptions;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;
import sdmxdl.DataStructureRef;
import sdmxdl.DataflowRef;
import sdmxdl.Key;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Collection;

/**
 * @author Philippe Charles
 */
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
public class DebugOutputOptions extends YamlOutputOptions {

    public void dump(Class<?> rootType, Object item) throws IOException {
        dump(toYaml(rootType), item);
    }

    public void dumpAll(Class<?> rootType, Collection<?> items) throws IOException {
        dumpAll(toYaml(rootType), items);
    }

    private static Yaml toYaml(Class<?> rootType) {
        DumperOptions opts = new DumperOptions();
        opts.setAllowReadOnlyProperties(true);
        return new Yaml(getRepresenter(rootType), opts);
    }

    private static Representer getRepresenter(Class<?> rootType) {
        Representer result = new Representer() {
            {
                this.representers.put(LocalDateTime.class, data -> representScalar(Tag.STR, ((LocalDateTime) data).toString()));
                this.representers.put(Key.class, data -> representScalar(Tag.STR, data.toString()));
                this.representers.put(DataflowRef.class, data -> representScalar(Tag.STR, data.toString()));
                this.representers.put(DataStructureRef.class, data -> representScalar(Tag.STR, data.toString()));
                this.representers.put(URL.class, data -> representScalar(Tag.STR, data.toString()));
            }
        };
        result.addClassTag(rootType, Tag.MAP);
        result.setPropertyUtils(YamlOutput.newLinkedPropertyUtils());
        result.getPropertyUtils().setBeanAccess(BeanAccess.FIELD);
        return result;
    }
}
