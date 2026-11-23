package sdmxdl.provider.dialects.icons;

import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import sdmxdl.provider.web.IconProviderSupport;
import sdmxdl.web.spi.IconProvider;

@DirectImpl
@ServiceProvider
public final class DialectsIconProvider implements IconProvider {

    @lombok.experimental.Delegate
    private final IconProviderSupport support = IconProviderSupport.builder()
            .id("DIALECTS_ICONS")
            .rank(100)
            .resources(DialectsIconProvider.class::getResourceAsStream)
            .build();
}
