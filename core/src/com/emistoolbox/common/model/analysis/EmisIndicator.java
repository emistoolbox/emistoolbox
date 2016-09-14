package com.emistoolbox.common.model.analysis;

import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.util.Named;

import java.util.List;
import java.util.Set;

public abstract interface EmisIndicator extends Named, EmisAggregatorList
{
	public String getGroupName(); 
	
	public void setGroupName(String groupName); 

	public String getYAxisLabel(); 

	public void setYAxisLabel(String yAxis); 
	
    public abstract double getGoodThreshold();

    public abstract String getGoodThresholdText();

    public abstract double getBadThreshold();

    public abstract String getBadThresholdText();

    public abstract boolean getBiggerIsBetter();

    public abstract void setThreshold(double paramDouble1, String paramString1, double paramDouble2, String paramString2);

    public abstract void setThreshold(double paramDouble, String paramString, boolean paramBoolean);

    public abstract double getMaxValue();

    public abstract void setMaxValue(double paramDouble);

    /** @return List of enums that can be used to split the result. */ 
    public abstract List<EmisMetaEnum> getSplitEnums();

    /** @return Verifies whether results can be added while splitting by the specified enum. */
    public abstract boolean isAddableResult(EmisMetaEnum split);
    
    public abstract Set<EmisMetaDateEnum> getUsedDateTypes(); 
}
