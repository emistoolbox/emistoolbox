package com.emistoolbox.common.renderer.pdfreport.impl;

import com.emistoolbox.common.renderer.pdfreport.PdfContentConfigVisitor;
import com.emistoolbox.common.renderer.pdfreport.PdfGisContentConfig;
import com.emistoolbox.common.results.GisMetaResult;
import java.io.Serializable;

public class PdfGisContentConfigImpl extends PdfMetaResultContentConfigImpl<GisMetaResult> implements PdfGisContentConfig, Serializable
{
	@Override
	public <T> T accept(PdfContentConfigVisitor<T> visitor) 
	{ return visitor.visit(this); }
}
