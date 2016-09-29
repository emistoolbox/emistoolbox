package com.emistoolbox.common.renderer.pdfreport.impl;

import com.emistoolbox.common.renderer.pdfreport.PdfContentConfigVisitor;
import com.emistoolbox.common.renderer.pdfreport.PdfTableContentConfig;
import com.emistoolbox.common.results.TableMetaResult;
import java.io.Serializable;

public class PdfTableContentConfigImpl extends PdfMetaResultContentConfigImpl<TableMetaResult> implements Serializable, PdfTableContentConfig
{
	@Override
	public <T> T accept(PdfContentConfigVisitor<T> visitor) 
	{ return visitor.visit(this); }
}
