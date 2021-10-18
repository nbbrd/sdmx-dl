package internal.sdmxdl.cli.ext;

import nbbrd.console.picocli.csv.CsvNewLine;
import nbbrd.console.picocli.csv.CsvOutput;
import nbbrd.console.picocli.text.TextOutputOptions;
import nbbrd.picocsv.Csv;
import picocli.CommandLine;

public class RFC4180OutputOptions extends TextOutputOptions implements CsvOutput {

    @CommandLine.Option(
            names = {"--CsvOutput-picocli-fix"},
            hidden = true
    )
    private boolean picocliFix;

    @Override
    public char getDelimiter() {
        return Csv.Format.RFC4180.getDelimiter();
    }

    @Override
    public CsvNewLine getSeparator() {
        return CsvNewLine.WINDOWS;
    }

    @Override
    public char getQuote() {
        return Csv.Format.RFC4180.getQuote();
    }

    @Override
    public char getComment() {
        return Csv.Format.RFC4180.getComment();
    }

    @Override
    public Csv.Format toFormat() {
        return Csv.Format.RFC4180;
    }
}
