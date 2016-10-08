package com.emistoolbox.client.ui.pdf;

import org.apache.commons.lang.StringUtils;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.client.admin.ui.EmisUtils;
import com.emistoolbox.client.util.ui.UIUtils;
import com.emistoolbox.common.ChartColor;
import com.emistoolbox.common.ChartFont;
import com.emistoolbox.common.renderer.pdfreport.layout.impl.CSSCreator;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;

public class ChartFontEditor extends HorizontalPanel implements EmisEditor<ChartFont>, HasValueChangeHandlers<ChartFont>
{
	private ChartFont font;
	
	private ChartColorEditor uiColor = new ChartColorEditor(); 
	private HTML uiText = new HTML("");

	private ListBox uiName = new ListBox(); 
	private IntPicker uiSize = new IntPicker(new int[] { 6, 8, 9, 10, 12, 14, 16, 18, 20, 24, 32 }, "", "px"); 
	private ListBox uiStyle = new ListBox(); 

	private PopupPanel uiPopup = new PopupPanel();
	private PushButton btnOk = new PushButton("OK"); 
	private PushButton btnCancel = new PushButton("Cancel"); 
	
	public ChartFontEditor()
	{
		setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		setSpacing(3); 
		
		uiPopup.setModal(true);
		uiPopup.setAutoHideEnabled(true);

		UIUtils.listBoxInit(uiName, ChartFont.FONTS);
		UIUtils.listBoxInit(uiStyle, new String[] { "plain", "bold", "italic", "bold+italic" });
		
		Grid uiGrid = new Grid(4, 2); 
		uiPopup.add(uiGrid);
		int row = 0; 
		uiGrid.setText(row, 0,  "Family");
		uiGrid.setWidget(row++, 1, uiName);

		uiGrid.setText(row, 0, "Size");
		uiGrid.setWidget(row++, 1, uiSize);
		
		uiGrid.setText(row, 0, "Style");
		uiGrid.setWidget(row++, 1, uiStyle);
		
		EmisUtils.init(btnOk, 40);
		EmisUtils.init(btnCancel, 40);
		HorizontalPanel hp = new HorizontalPanel(); 
		hp.setSpacing(3);

		hp.add(btnOk);
		hp.add(btnCancel);
		uiGrid.setWidget(row, 1, hp);

		btnOk.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (font == null)
					font = new ChartFont();
				
				font.setName(UIUtils.getListBoxValue(uiName));
				font.setSize(uiSize.get());
				font.setStyle(uiStyle.getSelectedIndex());

				updateUi(); 

				uiPopup.hide(); 
				ValueChangeEvent.fire(ChartFontEditor.this, font); 
			}
		}); 
		
		btnCancel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				uiPopup.hide(); 
			}
		}); 
		
		uiText.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				updateFontFields(); 
				uiPopup.showRelativeTo(uiText);
			}
		}); 
		
		
		// Current visible part. 
		add(uiColor); 
		add(uiText); 
		
		uiColor.addValueChangeHandler(new ValueChangeHandler<ChartColor>() {
			@Override
			public void onValueChange(ValueChangeEvent<ChartColor> event) {
				if (event.getValue() != null && font == null)
					font = new ChartFont(); 
				
				if (font != null)
					font.setColor(event.getValue());
				
				ChartFontEditor.this.updateUi(); 
				ValueChangeEvent.fire(ChartFontEditor.this, font); 
			}
		}); 
	}

	@Override
	public void commit() 
	{}

	@Override
	public ChartFont get() 
	{
		commit(); 
		return font;
	}

	@Override
	public void set(ChartFont font) 
	{
		this.font = font; 
		if (font == null)
			uiColor.set(null);
		else
			uiColor.set(font.getColor());
		
		updateUi(); 
	}
	
	private void updateUi()
	{
		if (font == null)
		{
			uiText.setText("(none)");
			return; 
		}

		if (font.getName() == null || font.getName().equals(""))
			font.setName(ChartFont.FONTS[0]);
		
		if (font.getSize() == 0)
			font.setSize(9);
		
		uiText.setText(font.getName() + " " + font.getSize() + "pt"); 
		Element tag = uiText.getElement(); 
		tag.setAttribute("style", CSSCreator.getCssAsString(font)); 
	}
	
	private void updateFontFields()
	{
		if (font != null)
		{
			UIUtils.setListBoxValue(uiName, font.getName());
			uiSize.set(font.getSize());
			uiStyle.setSelectedIndex(font.getStyle());
		}
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<ChartFont> handler) 
	{ return super.addHandler(handler, ValueChangeEvent.getType()); }
}
