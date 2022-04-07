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
package internal.sdmxdl.format.xml;

/**
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class Sdmxml {

    public static final Xmlns MESSAGE_V10 = Xmlns.of("http://www.SDMX.org/resources/SDMXML/schemas/v1_0/message");
    public static final Xmlns MESSAGE_V20 = Xmlns.of("http://www.SDMX.org/resources/SDMXML/schemas/v2_0/message");
    public static final Xmlns MESSAGE_V21 = Xmlns.of("http://www.sdmx.org/resources/sdmxml/schemas/v2_1/message");
}
