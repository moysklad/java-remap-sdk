package com.lognex.api.clients.documents;

import com.lognex.api.LognexApi;
import com.lognex.api.clients.ApiClient;
import com.lognex.api.clients.endpoints.ApiEndpoint;
import com.lognex.api.clients.endpoints.MetadataTemplatesEndpoint;
import com.lognex.api.entities.MetaEntity;
import com.lognex.api.utils.HttpRequestExecutor;
import com.lognex.api.utils.LognexApiException;

import java.io.IOException;

public class DocumentMetadataClient<T extends MetaEntity>
        extends ApiClient
        implements
        MetadataTemplatesEndpoint {

    private final Class<T> metaResponseEntityClass;

    public DocumentMetadataClient(LognexApi api, String path, Class<T> metaResponseEntityClass) {
        super(api, path + "metadata/");
        this.metaResponseEntityClass = metaResponseEntityClass;
    }

    @ApiEndpoint
    public T get() throws IOException, LognexApiException {
        return HttpRequestExecutor.
                path(api(), path()).
                get(metaEntityClass());
    }

    @Override
    public Class<T> metaEntityClass() {
        return metaResponseEntityClass;
    }
}
