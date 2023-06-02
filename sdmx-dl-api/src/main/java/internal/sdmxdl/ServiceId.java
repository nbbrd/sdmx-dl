package internal.sdmxdl;

import nbbrd.design.MightBePromoted;

import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
@MightBePromoted
public @interface ServiceId {
}
