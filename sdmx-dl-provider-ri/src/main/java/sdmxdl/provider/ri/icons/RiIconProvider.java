package sdmxdl.provider.ri.icons;

import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import sdmxdl.provider.web.IconProviderSupport;
import sdmxdl.web.spi.IconProvider;

@DirectImpl
@ServiceProvider
public final class RiIconProvider implements IconProvider {

    @lombok.experimental.Delegate
    private final IconProviderSupport support = IconProviderSupport.builder()
            .id("RI_ICONS")
            .rank(100)
            .resources(RiIconProvider.class::getResourceAsStream)
            .build();
}
