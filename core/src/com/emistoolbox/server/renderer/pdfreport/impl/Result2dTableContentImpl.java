package com.emistoolbox.server.renderer.pdfreport.impl;

import com.emistoolbox.common.results.Result;

public class Result2dTableContentImpl extends PdfResultTableContentImpl 
{
	private int topDimension = 1; 
	private int leftDimension = 0; 
	
	private String[] topHeaders; 
	private String[] leftHeaders; 
	
	@Override
	public void setResult(Result result) 
	{
		super.setResult(result);

		if (result.getDimensions() != 2)
			throw new IllegalArgumentException("Invalid result dimensions - expecting two"); 
	
		topHeaders = result.getHeadings(topDimension); 
		leftHeaders = result.getHeadings(leftDimension); 
	}

	@Override
	public int getColumns() 
	{ return getResult().getDimensionSize(topDimension) + 1; }

	@Override
	public int getRows() 
	{ return getResult().getDimensionSize(leftDimension) + 1; }

	@Override
	public String getText(int row, int col) 
	{
		if (row == 0 && col == 0)
			return ""; 

		if (row == 0)
			return topHeaders[col - 1]; 

		if (col == 0)
			return leftHeaders[row - 1]; 

		int[] index = new int[2]; 
		index[topDimension] = col - 1; 
		index[leftDimension] = row - 1; 

		return "" + getResult().get(index); 
	}
}
