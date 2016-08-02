/*
 * Copyright 2015 National Bank of Belgium
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

import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.Dimension;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
class Util {

    public static final int NO_FREQUENCY_CODE_ID_INDEX = -1;

    public static int getFrequencyCodeIdIndex(@Nonnull DataStructure dfs) {
        Dimension dimension = tryFindFreq(dfs.getDimensions());
        return dimension != null ? (dimension.getPosition() - 1) : NO_FREQUENCY_CODE_ID_INDEX;
    }

    private static Dimension tryFindFreq(Set<Dimension> list) {
        for (Dimension o : list) {
            switch (o.getId()) {
                case "FREQ":
                case "FREQUENCY":
                    return o;
            }
        }
        return null;
    }
}
