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

		setText(0, 0, "Page Layout"); 
		getCellFormatter().addStyleName(0, 0, "section");
		setWidget(1, 0, uiPageEditor); 
		
		uiTabs.add(uiPageList, "Pages");
		uiTabs.add(uiContentList, "New Content");
		uiTabs.add(uiReportProps, "Report"); 
		uiTabs.add(uiPageEditor.getPageConfigProps(), "Page");
		uiTabs.add(uiPageEditor.getFrameConfigProps(), "Frame");
		uiTabs.selectTab(0);
		setWidget(1, 1, uiTabs); 
				
		setWidth("100%"); 
		setHeight("100%"); 
		
		getCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_TOP);
		getCellFormatter().setVerticalAlignment(1, 1, HasVerticalAlignment.ALIGN_TOP);

		setCellSpacing(5);
		getColumnFormatter().setWidth(0, "70%");
		getColumnFormatter().setWidth(1, "30%");
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
