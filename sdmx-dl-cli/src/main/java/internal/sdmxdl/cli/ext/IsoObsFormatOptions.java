package internal.sdmxdl.cli.ext;

import nbbrd.console.picocli.text.ObsFormat;
import picocli.CommandLine;

import java.util.Locale;

public class IsoObsFormatOptions implements ObsFormat {

    @CommandLine.Option(
            names = {"--ObsFormat-picocli-fix"},
            hidden = true
    )
    private boolean picocliFix;

    @Override
    public Locale getLocale() {
        return Locale.ROOT;
    }

    @Override
    public String getDatePattern() {
        return "yyyy-MM-dd";
    }

    @Override
    public String getDatetimePattern() {
        return "yyyy-MM-dd'T'HH:mm:ss";
    }

    @Override
    public String getNumberPattern() {
        return "";
    }

    @Override
    public boolean isIgnoreNumberGrouping() {
        return true;
    }
}
