package com.emistoolbox.client.ui.pdf;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.client.admin.ui.EmisUtils;
import com.emistoolbox.client.admin.ui.GwtUtils;
import com.emistoolbox.client.ui.pdf.layout.LayoutFrameWidget;
import com.emistoolbox.client.ui.pdf.layout.LayoutPageEditor;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfText;
import com.emistoolbox.common.renderer.pdfreport.PdfVariableContentConfig;
import com.emistoolbox.common.renderer.pdfreport.impl.PdfTextContentConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.impl.PdfVariableContentConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutFrameConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPdfReportConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.impl.LayoutFrameConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.layout.impl.LayoutPageConfigImpl;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class LayoutPdfReportEditor extends FlexTable implements EmisEditor<LayoutPdfReportConfig> 
{
	private LayoutPdfReportConfig reportConfig; 
	private LayoutPageEditor uiPageEditor;
	
	private TabPanel uiTabs = new TabPanel(); 

	private IntPicker uiPageIndex = new IntPicker(new int[] { 1 }, "Page ", ""); 
	private Label uiPageIndexPost = new Label(""); 

	private ListBox uiMoveToPage = new ListBox(); 
	
	private int pageIndex = -1; 
	
	private PushButton btnPrevPage = new PushButton("<"); 
	private PushButton btnNextPage = new PushButton(">"); 
	private PushButton btnAddPage = new PushButton("Add Page"); 
	private PushButton btnDelPage = new PushButton("Del Page"); 

	private PushButton btnNewText = new PushButton("New Text Content"); 
	private PushButton btnNewVars = new PushButton("New Variable Content"); 

	private LayoutReportConfigProperties uiReportProps; 
	
	public LayoutPdfReportEditor(EmisMeta emis)
	{
		uiPageEditor = new LayoutPageEditor(this); 
		uiPageIndex.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				updateUi(uiPageIndex.getSelectedIndex());
				
			}
		}); 

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
		buttons.add(uiPageIndexPost);
		buttons.add(btnNextPage);
		
		hp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		hp.add(buttons);
		
		setWidget(0, 0, hp);
		setWidget(1, 0, uiPageEditor); 

		EmisUtils.init(btnNewText, 120); 
		EmisUtils.init(btnNewVars, 120); 
		VerticalPanel uiNewContent = new VerticalPanel(); 
		uiNewContent.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		uiNewContent.add(new HTML("Add a new text frame with title and body."));
		uiNewContent.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		uiNewContent.add(btnNewText);
		uiNewContent.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		uiNewContent.add(new HTML("<hr>Add a new list of variables of your location.")); 
		uiNewContent.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		uiNewContent.add(btnNewVars);
		uiNewContent.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		uiNewContent.add(new HTML("<hr><p>To add charts, tables or priortiy lists, go to the 'Analysis' tab, create a chart, table or priority list and then select <b>[Add to Report]</b>")); 
		
		btnNewText.addClickHandler(new ClickHandler() {
		@Override
			public void onClick(ClickEvent event) {
				addNewTextContent(); 
			}
		}); 
		
		btnNewVars.addClickHandler(new ClickHandler() {
		@Override
			public void onClick(ClickEvent event) {
				addNewVariableContent(); 
			}
		}); 
		
		uiReportProps = new LayoutReportConfigProperties(emis); 
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
		
		uiMoveToPage.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				int page = new Integer(GwtUtils.getListValue(uiMoveToPage));
				uiMoveToPage.setSelectedIndex(0); 
				if (page == -1)
				{
					uiPageEditor.removeFrame(uiPageEditor.getCurrentFrame());
					uiPageEditor.selectFrame(null);
				}
				else if (page >= 0)
				{
					moveFrame(uiPageEditor.getCurrentFrame(), page);
					uiPageEditor.selectFrame(null);
				}
			}
		}); 
		
		uiReportProps.addValueChangeHandler(new ValueChangeHandler<EmisPdfReportConfig>() {
			@Override
			public void onValueChange(ValueChangeEvent<EmisPdfReportConfig> event) {
				set(get()); 
			}
		});
	}
	
	public ListBox getMoveToPageUi()
	{ return uiMoveToPage; } 

	public void showPageProperties()
	{ uiTabs.selectTab(1); }

	public void showFrameProperties()
	{ uiTabs.selectTab(2); }
	
	private void addNewTextContent()
	{
		uiPageEditor.selectFrame(null);
		
		LayoutFrameConfig frame = new LayoutFrameConfigImpl(true);
		PdfTextContentConfigImpl textContent = new PdfTextContentConfigImpl(); 
		frame.setContentConfig(textContent);
		uiPageEditor.addFrame(frame, true);
	}
	
	private void addNewVariableContent()
	{
		if (reportConfig.getEntityType() == null)
			return; 
		
		uiPageEditor.selectFrame(null);
		
		final PdfVariableContentConfigImpl varContent = new PdfVariableContentConfigImpl(); 
		varContent.setEntity(reportConfig.getEntityType()); 
		
		PdfReportEditor.editVariables(varContent, new ValueChangeHandler<Boolean>() {

			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				LayoutFrameConfig frame = new LayoutFrameConfigImpl(); 
				frame.setContentConfig(varContent);
				uiPageEditor.addFrame(frame, true);
			}
		}); 
	}
	
	@Override
	public void commit()
	{
		if (uiPageEditor != null)
			uiPageEditor.commit(); 
		
		if (uiReportProps != null)
			uiReportProps.commit(); 
//		uiPageList.commit();
//		reportConfig.setPages(uiPageList.get()); 
	}

	@Override
	public LayoutPdfReportConfig get() 
	{
		commit(); 
		
		if (uiPageEditor != null)
			uiPageEditor.commit(); 
		
		if (uiReportProps != null)
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
			reportConfig.getPages().get(newPageIndex).addFrame(frame.get()); 
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
		if (reportConfig.getPages().size() > 0)
			uiPageEditor.set(reportConfig.getPages().get(pageIndex));

		updatePageIndex(pageIndex, reportConfig.getPages().size()); 

		int[] values = new int[reportConfig.getPages().size()];
		for (int i = 0; i < values.length; i++)
			values[i] = i + 1; 
		uiPageIndex.init(values, "Page ", "");
		uiPageIndexPost.setText(" of " + reportConfig.getPages().size());
		uiPageIndex.setSelectedIndex(indexOfPage);
		
		btnDelPage.setEnabled(pageIndex != -1);
		btnNextPage.setEnabled(pageIndex + 1 < reportConfig.getPages().size());
		btnPrevPage.setEnabled(pageIndex > 0);
	}
	
	private void updatePageIndex(int index, int total)
	{
		uiMoveToPage.clear(); 
		uiMoveToPage.addItem("", "-2");
		uiMoveToPage.addItem("(delete)", "-1");
		for (int i = 0; i < total; i++)
		{
			if (index != i)
				uiMoveToPage.addItem("Page " + (i + 1), "" + i); 
		}
		
		uiPageEditor.updatePageIndex(index, total); 
	}
}
