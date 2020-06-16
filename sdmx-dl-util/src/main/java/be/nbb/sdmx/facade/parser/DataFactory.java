/*
 * Copyright 2017 National Bank of Belgium
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
package be.nbb.sdmx.facade.parser;

import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.Frequency;
import internal.parser.DataFactories;
import java.time.LocalDateTime;
import nbbrd.io.text.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
//@ThreadSafe
public interface DataFactory {

    Freqs.@NonNull Parser getFreqParser(@NonNull DataStructure dsd);

    @NonNull
    Parser<LocalDateTime> getPeriodParser(@NonNull Frequency freq);

    @NonNull
    Parser<Double> getValueParser();

    @NonNull
    static DataFactory sdmx20() {
        return DataFactories.SDMX20;
    }

    @NonNull
    static DataFactory sdmx21() {
        return DataFactories.SDMX21;
    }
}
