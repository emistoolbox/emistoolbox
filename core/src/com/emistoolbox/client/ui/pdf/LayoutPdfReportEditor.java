package com.emistoolbox.client.ui.pdf;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.client.ui.pdf.layout.LayoutContentListEditor;
import com.emistoolbox.client.ui.pdf.layout.LayoutPageEditor;
import com.emistoolbox.client.ui.pdf.layout.LayoutPageListEditor;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPdfReportConfig;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Widget;

public class LayoutPdfReportEditor extends FlexTable implements EmisEditor<LayoutPdfReportConfig> 
{
	private LayoutPdfReportConfig reportConfig; 
	
	private LayoutPageListEditor uiPageList;
	private LayoutContentListEditor uiContentList;
	private LayoutPageEditor uiPageEditor;
	private Widget uiReportConfig; 
	
	public LayoutPdfReportEditor()
	{
		uiPageEditor = new LayoutPageEditor(); 
		uiPageList = new LayoutPageListEditor(uiPageEditor);

		setWidget(0, 0, uiPageList); 
		setWidget(1, 0, uiContentList); 
		setWidget(2, 0, uiReportConfig); 
		setWidget(0, 1, uiPageEditor); 
		
		setWidth("100%"); 
		setHeight("100%"); 
		setCellSpacing(5);
		getFlexCellFormatter().setRowSpan(0, 1, 3); 
		getColumnFormatter().setWidth(0, "25%");
		getColumnFormatter().setWidth(1, "75%");
		getCellFormatter().setHeight(0, 0, "40%"); 
		getCellFormatter().setHeight(1, 0, "40%"); 
		getCellFormatter().setHeight(2, 0, "20%"); 
	}
	
	@Override
	public void commit() 
	{}

	@Override
	public LayoutPdfReportConfig get() 
	{ return reportConfig; } 

	@Override
	public void set(LayoutPdfReportConfig reportConfig) 
	{ this.reportConfig = reportConfig; }
}
