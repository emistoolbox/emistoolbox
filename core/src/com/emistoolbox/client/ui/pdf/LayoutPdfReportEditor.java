package com.emistoolbox.client.ui.pdf;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.client.ui.pdf.layout.LayoutContentListEditor;
import com.emistoolbox.client.ui.pdf.layout.LayoutPageEditor;
import com.emistoolbox.client.ui.pdf.layout.LayoutPageListEditor;
import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig.PageSize;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPdfReportConfig;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class LayoutPdfReportEditor extends FlexTable implements EmisEditor<LayoutPdfReportConfig> 
{
	private LayoutPdfReportConfig reportConfig; 

	private LayoutPageListEditor uiPageList;
	private LayoutContentListEditor uiContentList;
	private LayoutPageEditor uiPageEditor;
	
	private TabPanel uiTabs = new TabPanel(); 

	private LayoutReportConfigProperties uiReportProps = new LayoutReportConfigProperties(); 
	
	public LayoutPdfReportEditor()
	{
		uiPageEditor = new LayoutPageEditor(); 
		uiContentList = new LayoutContentListEditor(); 
		uiPageList = new LayoutPageListEditor(uiPageEditor, uiContentList);

		getFlexCellFormatter().setColSpan(2, 0, 2); 
		getFlexCellFormatter().setRowSpan(0, 2, 3); 
		getCellFormatter().setVerticalAlignment(0, 2, HasVerticalAlignment.ALIGN_TOP);
		setWidget(0, 0, uiContentList); 
		setWidget(0, 1, uiPageList);  
		setHTML(1, 0, "<div class='section'>Page Layout</div>"); 
		setWidget(2, 0, uiPageEditor); 

		uiTabs.add(uiReportProps, "Report Config"); 
		uiTabs.add(uiPageEditor.getPageConfigProps(), "Page Config");
		uiTabs.add(uiPageEditor.getFrameConfigProps(), "Frame Config");
		setWidget(0, 2, uiTabs); 
				
		setWidth("100%"); 
		setHeight("100%"); 
		
		getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);
		getFlexCellFormatter().setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_TOP);
		getFlexCellFormatter().setVerticalAlignment(2, 0, HasVerticalAlignment.ALIGN_TOP);

		setCellSpacing(5);
		getColumnFormatter().setWidth(0, "50%");
		getColumnFormatter().setWidth(1, "50%");
	}
	
	@Override
	public void commit() 
	{
		uiPageEditor.commit(); 
		uiPageList.commit();
		uiContentList.commit();
		reportConfig.setUnusedContentConfigs(uiContentList.get()); 
		
		reportConfig.setPages(uiPageList.get()); 
	}

	@Override
	public LayoutPdfReportConfig get() 
	{
		commit(); 
		return reportConfig; 
	}
	
	@Override
	public void set(LayoutPdfReportConfig reportConfig) 
	{
		this.reportConfig = reportConfig; 
		
		uiPageEditor.setSizes(reportConfig.getPageSize(), reportConfig.getOrientation()); 
		uiPageList.set(reportConfig.getPages());
		uiContentList.set(reportConfig.getUnusedContentConfigs());
		
		uiReportProps.set(reportConfig);
	}
}
