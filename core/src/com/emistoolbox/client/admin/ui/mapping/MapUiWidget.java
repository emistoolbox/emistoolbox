package com.emistoolbox.client.admin.ui.mapping;

import com.emistoolbox.common.model.mapping.DbContext;
import com.emistoolbox.common.model.mapping.DbRowAccess;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.List;

public abstract interface MapUiWidget extends DbMetaInfoAware
{
    public abstract void setDbContext(DbContext paramDbContext);

    public abstract void setMetaContext(EmisMetaData paramEmisMetaData);

    public abstract EmisMetaData getMetaContext();

    public abstract DbRowAccess get();

    public abstract void set(DbRowAccess paramDbRowAccess);

    public abstract MapUiConstraint getConstraint();

    public abstract void setConstraint(MapUiConstraint paramMapUiConstraint);

    public abstract void getValuePreview(AsyncCallback<List<String>> paramAsyncCallback);
}
