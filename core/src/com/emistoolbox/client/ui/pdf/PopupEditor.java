package com.emistoolbox.client.ui.pdf;

import com.emistoolbox.client.admin.ui.EmisUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class PopupEditor extends PopupPanel implements HasValueChangeHandlers<PopupEditor>
{
	private FlexTable uiContainer = new FlexTable(); 
	private VerticalPanel uiPanel = new VerticalPanel(); 
	
	private PushButton btnOk = new PushButton("OK");
	private PushButton btnCancel = new PushButton("Cancel");

	public PopupEditor()
	{
		setModal(true);
		setAutoHideEnabled(true);
		
		EmisUtils.init(btnOk, 60); 
		EmisUtils.init(btnCancel, 60); 
		
		btnOk.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ValueChangeEvent.fire(PopupEditor.this, PopupEditor.this);
				hide(); 
			}
		}); 
		
		btnCancel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		
		HorizontalPanel hp = new HorizontalPanel(); 
		hp.setSpacing(3); 
		hp.add(btnOk);
		hp.add(btnCancel);
		
		uiPanel.add(uiContainer);
		uiPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		uiPanel.add(hp);
		
		setWidget(uiPanel); 
	}
	
	public void add(Widget w)
	{
		int row = uiContainer.getRowCount(); 
		uiContainer.setWidget(row, 0, w); 
		uiContainer.getFlexCellFormatter().setColSpan(row, 0, 2);
	}
	
	public void add(String html, Widget w)
	{
		int row = uiContainer.getRowCount(); 
		uiContainer.setHTML(row, 0, html); 
		uiContainer.setWidget(row, 1, w); 
	}
	
	
	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<PopupEditor> handler) 
	{ return addHandler(handler, ValueChangeEvent.getType()); }

	public void show(Widget anchor)
	{ showRelativeTo(anchor); }
}
