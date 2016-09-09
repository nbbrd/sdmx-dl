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

import be.nbb.sdmx.facade.util.Property.IntProperty;
import be.nbb.sdmx.facade.util.Property.LongProperty;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class CommonSdmxProperty {

    public static final IntProperty CONNECT_TIMEOUT = new IntProperty("connectTimeout");
    public static final IntProperty READ_TIMEOUT = new IntProperty("readTimeout");
    public static final LongProperty CACHE_TTL = new LongProperty("cacheTtl");

}
