package com.emistoolbox.server;

import com.emistoolbox.common.EmisReportModuleData;
import com.emistoolbox.server.model.EmisDataSet;
import java.io.Serializable;

class EmisReportModuleDataWithData extends EmisReportModuleData implements Serializable
{
    private static final long serialVersionUID = 1L;
    private EmisDataSet dataset = null;

    public EmisDataSet getDataset()
    {
        return this.dataset;
    }

    public void setDataset(EmisDataSet dataset)
    {
        this.dataset = dataset;
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.server.EmisReportModuleDataWithData JD-Core
 * Version: 0.6.0
 */