package sdmxdl.format;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
@Repeatable(ServiceSupport.List.class)
public @interface ServiceSupport {

    Class<?> value() default Void.class;

    @Documented
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.SOURCE)
    @interface List {

        ServiceSupport[] value();
    }
}
