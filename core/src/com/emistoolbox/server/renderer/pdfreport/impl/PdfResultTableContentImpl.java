package com.emistoolbox.server.renderer.pdfreport.impl;

import com.emistoolbox.common.results.Result;
import com.emistoolbox.server.renderer.pdfreport.PdfContentVisitor;
import com.emistoolbox.server.renderer.pdfreport.PdfContentWithResult;

public class PdfResultTableContentImpl extends PdfTableContentBase implements PdfContentWithResult
{
    private Result result;

    public Result getResult()
    { return result; } 
    
    public void setResult(Result result)
    { this.result = result; }

	@Override
	public <T> T accept(PdfContentVisitor<T> visitor) 
	{ return visitor.visit(this); } 
}
