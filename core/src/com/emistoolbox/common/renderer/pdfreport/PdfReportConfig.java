package com.emistoolbox.common.renderer.pdfreport;

import com.emistoolbox.common.model.meta.EmisMetaEntity;
import java.util.List;

public abstract interface PdfReportConfig extends EmisPdfReportConfig
{
    public abstract List<PdfContentConfig> getContentConfigs();

    public abstract void setContentConfigs(List<PdfContentConfig> paramList);

    public abstract void setLayout(int paramInt1, int paramInt2);

    public abstract int getRows();

    public abstract int getColumns();
}
