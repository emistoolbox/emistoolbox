package com.emistoolbox.server.renderer.pdfreport.impl;

import com.emistoolbox.common.results.Result;
import com.emistoolbox.server.renderer.pdfreport.PdfContentWithResult;
import com.emistoolbox.server.renderer.pdfreport.PdfTableContent;

public class PdfResultTableContentImpl extends PdfTableContentBase implements PdfContentWithResult
{
    private Result result;

    public Result getResult()
    { return result; } 
    
    public void setResult(Result result)
    { this.result = result; }
}
