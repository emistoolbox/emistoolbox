package com.emistoolbox.lib.pdf.specification;

public class TableFormat extends ContentFormat {
	TextFormat defaultSpecification;
	TextFormat [] [] textSpecifications;
	
	public TableFormat (int nrows,int ncolumns,TextFormat defaultSpecification) {
		this.defaultSpecification = defaultSpecification;
		textSpecifications = new TextFormat [ncolumns] [nrows];
	}

	public void setTextSpecification (int x,int y,TextFormat textSpecification) {
		textSpecifications [y] [x] = textSpecification;
	}
	
	public TextFormat getTextSpecification (int x,int y) {
		TextFormat textSpecification = textSpecifications [y] [x];
		return textSpecification != null ? textSpecification : defaultSpecification;
	}
}
