package com.emistoolbox.common;

import java.io.Serializable;

public class ChartStroke implements Serializable
{
	private float[] pattern; 
	
	public ChartStroke()
	{}
	
	public ChartStroke(float[] pattern)
	{ this.pattern = pattern; } 

	public float[] getPattern()
	{ return pattern; }
}

