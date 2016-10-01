package com.emistoolbox.client.ui.pdf;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.client.admin.ui.EmisUtils;
import com.emistoolbox.client.util.ui.UIUtils;
import com.emistoolbox.common.ChartFont;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;

public class ChartFontEditor extends HorizontalPanel implements EmisEditor<ChartFont>
{
	private ChartFont font;
	
	private ChartColorEditor uiColor; 
	private HTML uiText = new HTML("The quick brown fox");

	private ListBox uiName = new ListBox(); 
	private ListBox uiSize = new ListBox(); 
	private ListBox uiStyle = new ListBox(); 

	private PopupPanel uiPopup = new PopupPanel();
	private PushButton btnOk = new PushButton("OK"); 
	private PushButton btnCancel = new PushButton("Cancel"); 
	
	public ChartFontEditor()
	{
		UIUtils.listBoxInit(uiName, ChartFont.FONTS);
		UIUtils.listBoxInit(uiSize, new String[] { "6", "8", "9", "10", "12", "14", "16", "18", "20", "24", "32" });
		UIUtils.listBoxInit(uiStyle, new String[] { "plain", "bold", "italic", "bold+italic" });
		
		Grid uiGrid = new Grid(2, 4); 
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
				font.setName(UIUtils.getListBoxValue(uiName));
				font.setSize(new Integer(UIUtils.getListBoxValue(uiSize)));
				font.setStyle(uiStyle.getSelectedIndex());

				updateUi(); 
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
	}

	@Override
	public void commit() 
	{
	}

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
		uiColor.set(font.getColor());
		
		updateUi(); 
	}
	
	
	
	private void updateUi()
	{
		// TODO - update uiText from font CSS. 
	}
	
	private void updateFontFields()
	{
		UIUtils.setListBoxValue(uiName, font.getName());
		UIUtils.setListBoxValue(uiSize, "" + font.getSize());
		uiStyle.setSelectedIndex(font.getStyle());
	}
	
}

class FontStyleEditor extends Grid
{
	private ListBox uiName = new ListBox(); 
	private ListBox uiSize = new ListBox(); 
	private ListBox uiStyle = new ListBox(); 
	
	public FontStyleEditor()
	{
		super(2, 3);
		
		int row = 0;
		
		setText(row, 0, "Font Face:"); 
		setWidget(row++, 1, uiName); 
		
		setText(1, 0, "Size"); 
		setWidget(row++, 1, uiSize); 
		
		setText(2, 0, "Style"); 
		setWidget(row++, 1, uiStyle); 
	}
}
