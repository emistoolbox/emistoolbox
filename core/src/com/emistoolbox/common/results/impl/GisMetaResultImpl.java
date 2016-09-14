package com.emistoolbox.common.results.impl;

import com.emistoolbox.common.results.GisMetaResult;
import java.io.Serializable;

public class GisMetaResultImpl extends MetaResultImpl implements GisMetaResult, Serializable
{
    private static final long serialVersionUID = 1L;

	@Override
	public GisMetaResult createCopy() 
	{
		GisMetaResultImpl result = new GisMetaResultImpl(); 
		copy(result);

		return result; 
	}
}
