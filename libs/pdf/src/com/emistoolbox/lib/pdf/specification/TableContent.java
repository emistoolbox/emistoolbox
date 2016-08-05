package com.emistoolbox.lib.pdf.specification;

public class TableContent {
	String [] [] strings;
	
	public TableContent (int nrows,int ncolumns) {
		strings = new String [ncolumns] [nrows];
	}

	public void setText (int x,int y,String string) {
		strings [y] [x] = string;
	}
	
	public String getText (int x,int y) {
		String string = strings [y] [x];
		return string != null ? string : "";
	}
}
