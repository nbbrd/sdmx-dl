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
package sdmxdl.provider.connectors;

import it.bancaditalia.oss.sdmx.util.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
class ConnectorsConfigFix {

    static void fixConfiguration() {
//        Logger.getLogger(ConnectorRestClient.class.getName())
//                .log(Level.FINE, "Connectors config fix: before");

        Handler handler = addVoidHandlerBeforeInit();
        Map<Object, Object> sysProps = backupSystemPropertiesBeforeInit();
        Configuration.getConfiguration();
        restoreSystemPropertiesAfterInit(sysProps);
        removeVoidHandlerAfterInit(handler);

//        Logger.getLogger(ConnectorRestClient.class.getName())
//                .log(Level.FINE, "Connectors config fix: after");
    }

    private static Handler addVoidHandlerBeforeInit() {
        Handler result = new VoidHandler();
        Logger logger = Logger.getLogger("SDMX");
        logger.setUseParentHandlers(false);
        logger.addHandler(result);
        return result;
    }

    private static void removeVoidHandlerAfterInit(Handler handler) {
        Logger logger = Logger.getLogger("SDMX");
        logger.removeHandler(handler);
        logger.setUseParentHandlers(true);
    }

    private static Map<Object, Object> backupSystemPropertiesBeforeInit() {
        return new HashMap<>(System.getProperties());
    }

    private static void restoreSystemPropertiesAfterInit(Map<Object, Object> backup) {
        System.getProperties()
                .keySet()
                .stream()
                .filter(o -> !backup.containsKey(o))
                .collect(Collectors.toList())
                .forEach(System.getProperties()::remove);
        System.getProperties().putAll(backup);
    }

    private static final class VoidHandler extends Handler {

        @Override
        public void publish(LogRecord lr) {
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }
}
