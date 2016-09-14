package com.emistoolbox.client.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.emistoolbox.client.Message;
import com.emistoolbox.client.admin.ui.EmisUtils;
import com.emistoolbox.client.util.ui.CenteredPositionCallback;
import com.emistoolbox.common.model.EmisEnumSet;
import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.analysis.impl.Context;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaData.EmisDataType;
import com.emistoolbox.common.util.NamedUtil;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class GlobalFilterUi extends HorizontalPanel implements HasValueChangeHandlers<EmisContext>
{
	private HTML uiTitle = new HTML(); 
	private HTML uiValues = new HTML(); 
	
	private EmisContext value; 
	private EmisMeta meta; 

	private List<HandlerRegistration> handlers = new ArrayList<HandlerRegistration>(); 
	
	public GlobalFilterUi()
	{
		ClickHandler handler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) 
			{
				if (meta == null)
					return; 
				
				final PopupPanel popup = new PopupPanel(); 
				popup.setSize("600px", "400px");
				popup.setModal(true); 
				popup.setAutoHideEnabled(true);
				
				final GlobalFilterEditorUi editor = new GlobalFilterEditorUi(meta); 
				editor.set(get()); 
				
				PushButton btnOk = new PushButton("OK"); 
				EmisUtils.init(btnOk, 80); 
				btnOk.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) 
					{
						EmisContext value = editor.get(); 
						set(value); 
						
						ValueChangeEvent.fire(GlobalFilterUi.this, ((Context) value).createCopy());
						
						popup.hide(); 
					}
				}); 

				PushButton btnCancel = new PushButton("Cancel");
				EmisUtils.init(btnCancel, 80); 
				btnCancel.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) 
					{
						popup.hide(); 
					}
				}); 
				
				HorizontalPanel hp = new HorizontalPanel(); 
				hp.setSpacing(3); 
				hp.add(btnOk);
				hp.add(btnCancel); 
				
				VerticalPanel uiLayout = new VerticalPanel();
				HTML title = new HTML(Message.messageReport().globalFilterTitle());
				title.setStylePrimaryName("title"); 
				uiLayout.add(title);
				uiLayout.add(editor); 
				uiLayout.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
				uiLayout.add(hp); 
				
				ScrollPanel scroll = new ScrollPanel(); 
				scroll.setWidget(uiLayout);
				popup.setWidget(scroll);
				popup.setPopupPositionAndShow(new CenteredPositionCallback(popup));
			}
		};
		
		uiTitle.addClickHandler(handler); 
		uiValues.addClickHandler(handler); 
		
		add(uiTitle); 
		add(uiValues); 
	}
	
	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<EmisContext> handler) 
	{
		HandlerRegistration reg = addHandler(handler, ValueChangeEvent.getType());
		synchronized (handlers) {			
			handlers.add(reg); 
		}
		return reg; 
	}

	public synchronized void resetValueChangeHandlers()
	{
		synchronized (handlers) {			
			for (HandlerRegistration handler : handlers)
				handler.removeHandler(); 
			
			handlers.clear(); 
		}
	}
	
	public void set(EmisContext context)
	{
		this.value = context; 
		updateUi();
	}
	
	public EmisContext createCopy()
	{ return value == null ? null : ((Context) value).createCopy(); }
	
	public EmisContext get()
	{ return value; }

	public void setMeta(EmisMeta meta)
	{
		this.meta = meta; 
		uiTitle.setHTML("<b>" + Message.messageReport().globalFilterTitle() + "</b>: ");
		uiValues.setHTML(Message.messageReport().globalFilterNone());
		
		value = null; 
		updateUi(); 
	} 
	
	private void addVariableText(StringBuffer result, String name)
	{
		if (result.length() > 0)
			result.append("; "); 
		
		result.append(name); 
		result.append("="); 
	}
	
	private void addFilterText(StringBuffer result, Collection<EmisEnumSet> values)
	{
		for (EmisEnumSet value : values)
		{
			addVariableText(result, value.getEnum().getName()); 
			appendEnumSet(result, value);  
		}
	}
	
	private void updateUi()
	{
		if (value == null)
		{
			uiValues.setHTML(Message.messageReport().globalFilterNone()); 
			return; 
		}
		else
		{
			StringBuffer result = new StringBuffer(); 

			if (value.getDateEnumFilters() != null)
				addFilterText(result, value.getDateEnumFilters().values()); 

			if (value.getEnumFilters() != null)
				addFilterText(result, value.getEnumFilters().values());  
			
			for (EmisMetaEntity entityType : meta.getEntities())
			{
				for (String fieldName : value.getEntityFilterNames(entityType))
				{
					EmisMetaData field = NamedUtil.find(fieldName, entityType.getData());  
					if (field == null)
						continue; 

					byte[] indexes = value.getEntityFilterValues(field); 
					if (value == null)
						continue; 

					addVariableText(result, entityType.getName() + "." + field.getName()); 
					if (field.getType() == EmisDataType.BOOLEAN)
						result.append(indexes[0] == 1 ? "true" : "false"); 
					else
						appendEnumSet(result, GlobalFilterEditorUi.getEnumSet(field.getEnumType(), indexes)); 
				}
			}
			
			if (result.length() == 0)
				uiValues.setHTML(Message.messageReport().globalFilterNone());
			else
				uiValues.setHTML(result.toString()); 
		}
	}
	
	private void appendEnumSet(StringBuffer result, EmisEnumSet values)
	{
		String delim = ""; 
		for (String v : values.getAll())
		{
			result.append(delim); 
			result.append(v); 
			delim = ",";
		}
	}
}
