package com.emistoolbox.server.renderer.pdfreport.impl;

import com.emistoolbox.common.results.Result;

public class Result1dTableContentImpl extends PdfResultTableContentImpl 
{
	private String[] headers; 
	
	@Override
	public void setResult(Result result) 
	{
		if (result.getDimensions() != 1)
			throw new IllegalArgumentException("Invalid result dimensions - expecting only one"); 
	
		headers = result.getHeadings(0); 

		super.setResult(result);
	}

	@Override
	public int getColumns() 
	{ return 2; }

	@Override
	public int getRows() 
	{ return getResult().getDimensionSize(0) + 1; }

	@Override
	public String getText(int row, int col) 
	{
		if (row == 0)
			return col == 0 ? "" : getResult().getValueLabel(); 
		
		if (col == 0)
			return headers[row - 1]; 
		else
			return "" + getResult().get(new int[] { row -  1}); 
	}
}
