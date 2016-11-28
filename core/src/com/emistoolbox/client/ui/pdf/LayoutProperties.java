package com.emistoolbox.client.ui.pdf;

import java.util.HashMap;
import java.util.Map;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.client.admin.ui.ListBoxWithUserObjects;
import com.emistoolbox.common.ChartFont;
import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfText;
import com.emistoolbox.common.renderer.pdfreport.TextSet;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutFrameConfig;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.VerticalPanel;

public abstract class LayoutProperties<T> extends FlexTable implements EmisEditor<T>, HasValueChangeHandlers<T> 
{
	private LayoutPdfReportEditor reportEditor; 
	
	public LayoutProperties(LayoutPdfReportEditor editor)
	{ reportEditor = editor; } 
	
	public LayoutPdfReportEditor getReportEditor()
	{ return reportEditor; } 
	
	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<T> handler) 
	{ return addHandler(handler, ValueChangeEvent.getType()); }

	protected void fireValueChangeEvent()
	{ ValueChangeEvent.fire(this, get()); }
	
	private ChangeHandler handler = null; 
	protected ChangeHandler getChangeHandler()
	{ 
		if (handler == null)
			handler = new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					fireValueChangeEvent(); 
				}
			};
		
		return handler; 
	}
	
	private ValueChangeHandler valueHandler = null;
	
	protected ValueChangeHandler getValueChangeHandler()
	{
		if (valueHandler == null)
			valueHandler = new ValueChangeHandler() {
				@Override
				public void onValueChange(ValueChangeEvent event) {
					fireValueChangeEvent(); 
				}
			};
			
		return valueHandler; 
	}
	
	protected Map<String, TextSetEntryUi> initTexts(String[] keys, String[] labels)
	{
		Map<String, TextSetEntryUi> result = new HashMap<String, TextSetEntryUi>(); 

		int row = getRowCount();
		for (int i = 0; i < keys.length; i++)
		{
			TextSetEntryUi ui = new TextSetEntryUi(); 
			ui.addValueChangeHandler(getValueChangeHandler(), getChangeHandler()); 
			result.put(keys[i], ui); 

			setWidget(row, 1, ui); 
			setText(row, 0, labels == null || labels.length <= i ? keys[i] : labels[i]);
			getRowFormatter().setVerticalAlign(row, HasVerticalAlignment.ALIGN_TOP);
			
			row++; 
		}
		
		return result; 
	}
	
	protected void updateTexts(EmisPdfReportConfig reportConfig, LayoutFrameConfig config, Map<String, TextSetEntryUi> uis)
	{
		setTextSet(uis, config); 
		
		for (Map.Entry<String, TextSetEntryUi> entry : uis.entrySet())
		{
			TextSetEntryUi ui = entry.getValue(); 
			ui.setConfigs(reportConfig, config == null ? null : config.getContentConfig());
		}
	}
	
	protected void setTextSet(Map<String, TextSetEntryUi> ui, TextSet texts)
	{
		for (Map.Entry<String, TextSetEntryUi> entry : ui.entrySet())
		{
			String key = entry.getKey(); 
			if (texts == null)
			{
				entry.getValue().setText("");
				entry.getValue().setFont(ChartFont.DEFAULT_FONT); 
			}
			else
			{
				entry.getValue().setText(texts.getText(key));
				entry.getValue().setFont(texts.getFont(key)); 
			}
		}
	}
	
	protected void updateTextSet(Map<String, TextSetEntryUi> ui, TextSet texts)
	{
		for (Map.Entry<String, TextSetEntryUi> entry : ui.entrySet())
			texts.putText(entry.getKey(), entry.getValue().getText(), entry.getValue().getFont(), entry.getValue().getAlignment());  
	}
	
	public static class TextSetEntryUi extends VerticalPanel
	{
		private TextBoxBase uiText;  
		private ChartFontEditor uiFont = new ChartFontEditor(); 
		private ListBoxWithUserObjects<String> uiAlign = new ListBoxWithUserObjects<String>(); 
		private HTML uiAddVariable = new HTML("<small>[add variable]</small>");
		
		private EmisPdfReportConfig reportConfig;  
		private PdfContentConfig contentConfig;
		
		public TextSetEntryUi()
		{ this(false); }

		public TextSetEntryUi(boolean asTextArea)
		{
			uiText = asTextArea ? new TextArea() : new TextBox(); 

			uiAlign.add(PdfText.ALIGN_LEFT, PdfText.ALIGN_LEFT);
			uiAlign.add(PdfText.ALIGN_CENTER, PdfText.ALIGN_CENTER);
			uiAlign.add(PdfText.ALIGN_RIGHT, PdfText.ALIGN_RIGHT);
			
			HorizontalPanel hp = new HorizontalPanel(); 
			hp.setWidth("100%");
			hp.add(uiAddVariable);
			hp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
			hp.add(uiAlign);

			add(uiText);
			add(hp); 
			add(uiFont);			
			
			uiAddVariable.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					final PopupPanel popup = new PopupPanel();
					popup.setHeight("300px");
					popup.setWidth("150px");
					ScrollPanel panel = new ScrollPanel(); 
					
					TextVariablePicker picker = new TextVariablePicker(reportConfig, contentConfig);
					popup.setModal(true); 
					popup.setAutoHideEnabled(true);
					popup.add(panel);
					popup.showRelativeTo(uiAddVariable);
					
					panel.add(picker);
					
					picker.addValueChangeHandler(new ValueChangeHandler<String>() {
						@Override
						public void onValueChange(ValueChangeEvent<String> event) 
						{
							insert("{" + event.getValue() + "}"); 
							popup.hide(); 
						}
					}); 
				}
			}); 
		}
		
		private void insert(String text)
		{
			int pos = uiText.getCursorPos();
			String current = uiText.getText(); 
			uiText.setText(current.substring(0, pos) + text + current.substring(pos));
		}
		
		public void setConfigs(EmisPdfReportConfig reportConfig, PdfContentConfig contentConfig)
		{
			this.reportConfig = reportConfig; 
			this.contentConfig = contentConfig; 
		}
		
		public void setVisible(boolean showText, boolean showFont)
		{
			uiText.setVisible(showText); 
			uiFont.setVisible(showFont);
		}
		
		public void addValueChangeHandler(ValueChangeHandler handler, ChangeHandler changeHandler)
		{
			uiText.addValueChangeHandler(handler); 
			uiFont.addValueChangeHandler(handler); 
			uiAlign.addChangeHandler(changeHandler); 
		}
		
		public void setText(String text)
		{ uiText.setText(text); }
		
		public void setFont(ChartFont font)
		{ uiFont.set(font); }
		
		public void setAlignment(String align)
		{
			if (align == null)
				align = PdfText.ALIGN_LEFT; 

			uiAlign.setValue(align);
		}
		
		public String getText()
		{ return uiText.getText(); } 
		
		public ChartFont getFont()
		{ return uiFont.get(); } 
		
		public String getAlignment()
		{ return uiAlign.getUserObject(); } 
		
		public TextBoxBase getTextUi()
		{ return uiText; } 
		
		public ChartFontEditor getFontUi()
		{ return uiFont; } 
	}
}
