package com.emistoolbox.common.renderer.pdfreport.impl;

import java.io.Serializable;

import com.emistoolbox.common.renderer.pdfreport.PdfContentConfigVisitor;
import com.emistoolbox.common.renderer.pdfreport.PdfPriorityListContentConfig;
import com.emistoolbox.common.results.PriorityMetaResult;

public class PdfPriorityListContentConfigImpl extends PdfMetaResultContentConfigImpl<PriorityMetaResult> implements PdfPriorityListContentConfig, Serializable
{
	@Override
	public <T> T accept(PdfContentConfigVisitor<T> visitor) 
	{ return visitor.visit(this); }
}
