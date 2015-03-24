package com.emistoolbox.client.ui.analysis;

import com.emistoolbox.client.admin.ui.EmisUtils;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class FilterPanel<T extends Widget> extends VerticalPanel 
{
	public enum FILTER_STATE { WITH_FILTER, IGNORE_FILTER, NONE }; 
	
	private ToggleButton btnAdd = new ToggleButton("Filter"); 
	private ToggleButton btnIgnore = new ToggleButton("Ignore"); 
	private ToggleButton btnClear = new ToggleButton("None"); 
	
	private FILTER_STATE[] buttonStates = new FILTER_STATE[] { FILTER_STATE.WITH_FILTER, FILTER_STATE.NONE, FILTER_STATE.IGNORE_FILTER }; 
	private ToggleButton[] buttons = new ToggleButton[] { btnAdd, btnClear , btnIgnore}; 
	
	private FILTER_STATE state = FILTER_STATE.NONE; 
	private T editor; 
	
	private EmisMetaData field; 
	private EmisMetaEnum enumType; 
	
	public FilterPanel(EmisMetaData field, EmisMetaEnum enumType, T editor)
	{
		this.editor = editor; 
		this.field = field; 
		this.enumType = enumType; 
		
		HorizontalPanel hp = new HorizontalPanel();
		for (int i = 0; i < buttons.length; i++)
		{
			final FILTER_STATE buttonState = buttonStates[i]; 
			
			buttons[i].addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					updateUi(buttonState);
				}
			}); 
			EmisUtils.initSmall(buttons[i], 60); 
			hp.add(buttons[i]);
		}
		
		add(hp);
		add(editor);
		
		updateUi(FILTER_STATE.NONE);
	}
	
	public T getEditor()
	{ return this.editor; }

	public EmisMetaData getField()
	{ return field; } 
	
	public EmisMetaEnum getEnumType()
	{ return enumType; } 
	
	public FILTER_STATE getFilterState()
	{ return state; } 
	
	public boolean hasFilter()
	{ return state == FILTER_STATE.WITH_FILTER; } 
	
	public boolean hasIgnoreFilter()
	{ return state == FILTER_STATE.IGNORE_FILTER; } 
	
	public void updateUi(FILTER_STATE newState)
	{
		state = newState; 
		for (int i = 0; i < buttons.length; i++)
			buttons[i].setDown(state == buttonStates[i]); 
		
		editor.setVisible(state == FILTER_STATE.WITH_FILTER);
	}
}
