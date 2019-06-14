package com.emistoolbox.common.renderer.pdfreport.impl;

import com.emistoolbox.client.admin.ui.EmisUtils;
import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.analysis.EmisIndicator;
import com.emistoolbox.common.model.impl.EmisEnumUtils;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.renderer.pdfreport.PdfMetaResultContentConfig;
import com.emistoolbox.common.results.MetaResult;
import com.emistoolbox.common.results.MetaResultDimensionUtil;
import com.emistoolbox.common.results.MetaResultValue;

import java.io.Serializable;

public abstract class PdfMetaResultContentConfigImpl<T extends MetaResult> extends PdfContentConfigImpl implements PdfMetaResultContentConfig<T>, Serializable
{
    private T metaResult;

    public T getMetaResult()
    { return this.metaResult; }

    public void setMetaResult(T metaResult)
    { this.metaResult = metaResult; }

    public String getInfo()
    { return MetaResultDimensionUtil.getTitle(getMetaResult(), MetaResultDimensionUtil.ENTITY_DATE_LEVEL.GENERIC, false); }

    public EmisMetaEntity getSeniorEntity()
    { return metaResult == null ? null : metaResult.getSeniorEntity(); }
}
