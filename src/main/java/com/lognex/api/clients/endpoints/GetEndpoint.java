package com.lognex.api.clients.endpoints;

import com.lognex.api.entities.MetaEntity;
import com.lognex.api.utils.HttpRequestExecutor;
import com.lognex.api.utils.LognexApiException;
import com.lognex.api.utils.params.ApiParam;

import java.io.IOException;

public interface GetEndpoint<T extends MetaEntity> extends Endpoint {
    @ApiEndpoint
    default T get(ApiParam... params) throws IOException, LognexApiException {
        return HttpRequestExecutor.
                path(api(), path()).
                apiParams(params).
                get((Class<T>) entityClass());
    }
}
