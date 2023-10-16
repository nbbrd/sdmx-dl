package sdmxdl;

import nbbrd.design.SealedType;
import sdmxdl.file.FileSource;
import sdmxdl.web.WebSource;

@SealedType({
        FileSource.class,
        WebSource.class
})
public abstract class Source {

}
