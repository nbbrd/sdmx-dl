package sdmxdl.provider.web;

import lombok.NonNull;

public interface RestClientDecorator extends RestClient {

    @NonNull
    RestClient getDecorated();
}
