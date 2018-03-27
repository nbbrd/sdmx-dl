/*
 * Copyright 2016 National Bank of Belgium
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
package be.nbb.sdmx.facade.util;

import java.util.Map;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class Property {

    public boolean get(String key, boolean defaultValue, Map props) {
        Object result = props.get(key);
        if (result != null) {
            return Boolean.parseBoolean(result.toString());
        }
        return defaultValue;
    }

    public int get(String key, int defaultValue, Map props) {
        Object result = props.get(key);
        if (result != null) {
            try {
                return Integer.parseInt(result.toString());
            } catch (NumberFormatException ex) {
                // do nothing
            }
        }
        return defaultValue;
    }

    public long get(String key, long defaultValue, Map props) {
        Object result = props.get(key);
        if (result != null) {
            try {
                return Long.parseLong(result.toString());
            } catch (NumberFormatException ex) {
                // do nothing
            }
        }
        return defaultValue;
    }

    public void set(String key, boolean value, Map props) {
        props.put(key, String.valueOf(value));
    }

    public void set(String key, int value, Map props) {
        props.put(key, String.valueOf(value));
    }

    public void set(String key, long value, Map props) {
        props.put(key, String.valueOf(value));
    }
}
