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

import nbbrd.console.picocli.csv.CsvOutputOptions;
import nbbrd.picocsv.Csv;
import picocli.CommandLine;

import java.nio.charset.Charset;

/**
 * @author Philippe Charles
 */
@lombok.Data
public final class Excel {

    @CommandLine.Option(
            names = {"-x", "--excel-compatibility"},
            hidden = true,
            defaultValue = "false"
    )
    private boolean excelCompatibility;

    public void apply(CsvOutputOptions csv) {
        if (excelCompatibility) {
            csv.setDelimiter(Csv.Format.EXCEL.getDelimiter());
            csv.setQuote(Csv.Format.EXCEL.getQuote());
            csv.setSeparator(Csv.Format.EXCEL.getSeparator());
            csv.setEncoding(Charset.defaultCharset());
        }
    }

    public void apply(ObsFormatOptions format) {
        if (excelCompatibility) {
            format.setLocale(null);
            format.setDatetimePattern("yyyy-MM-dd HH:mm:ss");
            format.setNumberPattern(null);
            format.setIgnoreNumberGrouping(true);
        }
    }
}
