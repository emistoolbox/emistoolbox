package com.emistoolbox.client.ui.pdf;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.client.admin.ui.EmisUtils;
import com.emistoolbox.client.ui.pdf.layout.LayoutFrameWidget;
import com.emistoolbox.client.ui.pdf.layout.LayoutPageEditor;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPdfReportConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.impl.LayoutPageConfigImpl;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class LayoutPdfReportEditor extends FlexTable implements EmisEditor<LayoutPdfReportConfig> 
{
	private LayoutPdfReportConfig reportConfig; 
	private LayoutPageEditor uiPageEditor;
	
	private TabPanel uiTabs = new TabPanel(); 
	private Label uiPageIndex = new Label(""); 

	private int pageIndex = -1; 
	
	private PushButton btnPrevPage = new PushButton("<"); 
	private PushButton btnNextPage = new PushButton(">"); 
	private PushButton btnAddPage = new PushButton("Add Page"); 
	private PushButton btnDelPage = new PushButton("Del Page"); 

	private PushButton btnNewText = new PushButton("New Text Content"); 
	private PushButton btnNewVars = new PushButton("New Variable Content"); 

	private LayoutReportConfigProperties uiReportProps = new LayoutReportConfigProperties(); 
	
	public LayoutPdfReportEditor()
	{
		uiPageEditor = new LayoutPageEditor(this); 
//		uiPageList = new LayoutPageListEditor(uiPageEditor);

		Label title = new Label("Page Layout");
		title.setStyleName("sectionBlue");
		
		EmisUtils.initSmall(btnAddPage, 60);
		EmisUtils.initSmall(btnDelPage, 60);
		EmisUtils.initSmall(btnPrevPage, 20);
		EmisUtils.initSmall(btnNextPage, 20); 
		
		btnPrevPage.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) 
			{ updateUi(pageIndex - 1); }
		}); 

		btnNextPage.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) 
			{ updateUi(pageIndex + 1); }
		}); 

		btnAddPage.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				reportConfig.getPages().add(new LayoutPageConfigImpl());
				updateUi(reportConfig.getPages().size() - 1); 
			}
		}); 

		btnDelPage.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				reportConfig.getPages().remove(pageIndex); 
				updateUi(pageIndex); 
			}
		}); 
		
		HorizontalPanel hp = new HorizontalPanel(); 
		hp.setWidth("100%");
		hp.add(title);

		HorizontalPanel buttons = new HorizontalPanel(); 
		buttons.setSpacing(3);
		buttons.add(btnAddPage);
		buttons.add(btnDelPage);
		buttons.add(new HTML(" | "));
		buttons.add(btnPrevPage);
		buttons.add(uiPageIndex);
		buttons.add(btnNextPage);
		
		hp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		hp.add(buttons);
		
		setWidget(0, 0, hp);
		setWidget(1, 0, uiPageEditor); 

		EmisUtils.init(btnNewText, 90); 
		EmisUtils.init(btnNewVars, 90); 
		VerticalPanel uiNewContent = new VerticalPanel(); 
		uiNewContent.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		uiNewContent.add(new HTML("Add a new text frame with title and body."));
		uiNewContent.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		uiNewContent.add(btnNewText);
		uiNewContent.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		uiNewContent.add(new HTML("Add a new list of variables of your location.")); 
		uiNewContent.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		uiNewContent.add(btnNewVars);
		uiNewContent.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		uiNewContent.add(new HTML("<p>To add charts, tables or priortiy lists, go to the 'Analysis' tab, create a chart, table or priority list and then select <b>[Add to Report]</b>")); 
		
		//		uiTabs.add(uiPageList, "Pages");
		uiTabs.add(uiReportProps, "Report"); 
		uiTabs.add(uiPageEditor.getPageConfigProps(), "Page");
		uiTabs.add(uiPageEditor.getFrameConfigProps(), "Frame");
		uiTabs.add(uiNewContent, "New Content");
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
		uiReportProps.commit(); 
//		uiPageList.commit();
//		reportConfig.setPages(uiPageList.get()); 
	}

	@Override
	public LayoutPdfReportConfig get() 
	{
		commit(); 
		uiPageEditor.commit(); 
		uiReportProps.commit(); 
		
		return reportConfig; 
	}
	
	@Override
	public void set(LayoutPdfReportConfig reportConfig) 
	{
		this.reportConfig = reportConfig; 
		
		uiPageEditor.setSizes(reportConfig.getPageSize(), reportConfig.getOrientation()); 
		uiReportProps.set(reportConfig);
//		uiPageList.set(reportConfig.getPages());

		updateUi(0); 
	}
	
	public void moveFrame(LayoutFrameWidget frame, Integer newPageIndex)
	{
		uiPageEditor.removeFrame(frame); 
		if (newPageIndex != null)
		{
			reportConfig.getPages().get(newPageIndex).addFrame(frame.get()); 
			updateUi(newPageIndex); 
		}
	}
	
	private void updateUi(int indexOfPage)
	{
		if (reportConfig.getPages().size() == 0)
			reportConfig.getPages().add(new LayoutPageConfigImpl()); 
		
		if (indexOfPage < 0)
			indexOfPage = 0; 
		
		if (indexOfPage >= reportConfig.getPages().size())
			indexOfPage = -1; 
		
		if (pageIndex != -1)
			uiPageEditor.commit();
		
		pageIndex = indexOfPage; 
		uiPageEditor.set(reportConfig.getPages().get(pageIndex));
		uiPageEditor.updatePageIndex(pageIndex, reportConfig.getPages().size()); 

		uiPageIndex.setText("Page " + (pageIndex + 1) + " of " + reportConfig.getPages().size());
		btnDelPage.setEnabled(pageIndex != -1);
		btnNextPage.setEnabled(pageIndex + 1 < reportConfig.getPages().size());
		btnPrevPage.setEnabled(pageIndex > 0);
	}
}
			