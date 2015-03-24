package com.emistoolbox.server.renderer.pdfreport;

import com.emistoolbox.common.results.Result;

public abstract interface PdfResultContent extends PdfContent
{
    public abstract void setResult(Result paramResult);
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.server.renderer.pdfreport.PdfResultContent
 * JD-Core Version: 0.6.0
 */