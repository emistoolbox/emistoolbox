package com.emistoolbox.server.renderer.pdfreport;

import com.emistoolbox.common.results.Result;

public interface PdfContentWithResult
{
	public Result getResult();
	public void setResult(Result result);
}
