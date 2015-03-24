package com.emistoolbox.common.renderer.pdfreport;

import com.emistoolbox.common.results.MetaResult;
import com.google.gwt.user.client.rpc.IsSerializable;

public abstract interface PdfMetaResultContentConfig<T extends MetaResult> extends PdfContentConfig, IsSerializable
{
    public abstract void setMetaResult(T paramT);

    public abstract T getMetaResult();
}
