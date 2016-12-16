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
public abstract class Property {

    private final String key;

    public Property(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public static final class BoolProperty extends Property {

        public BoolProperty(String key) {
            super(key);
        }

        public boolean get(Map props, boolean defaultValue) {
            Object result = props.get(getKey());
            if (result != null) {
                return Boolean.parseBoolean(result.toString());
            }
            return defaultValue;
        }

        public void set(Map<String, String> props, boolean value) {
            props.put(getKey(), String.valueOf(value));
        }
    }

    public static final class IntProperty extends Property {

        public IntProperty(String key) {
            super(key);
        }

        public int get(Map props, int defaultValue) {
            Object result = props.get(getKey());
            if (result != null) {
                try {
                    return Integer.parseInt(result.toString());
                } catch (NumberFormatException ex) {
                    // do nothing
                }
            }
            return defaultValue;
        }

        public void set(Map<String, String> props, int value) {
            props.put(getKey(), String.valueOf(value));
        }
    }

    public static final class LongProperty extends Property {

        public LongProperty(String key) {
            super(key);
        }

        public long get(Map props, long defaultValue) {
            Object result = props.get(getKey());
            if (result != null) {
                try {
                    return Long.parseLong(result.toString());
                } catch (NumberFormatException ex) {
                    // do nothing
                }
            }
            return defaultValue;
        }

        public void set(Map<String, String> props, long value) {
            props.put(getKey(), String.valueOf(value));
        }
    }
}
