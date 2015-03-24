package com.emistoolbox.client.admin.ui.mapping;

import com.emistoolbox.client.EmisToolboxServiceAsync;
import com.emistoolbox.client.admin.ui.EmisUtils;
import com.emistoolbox.client.ui.fileDialog.FileListDialogBox;
import com.emistoolbox.client.util.ui.CenteredPositionCallback;
import com.emistoolbox.common.model.mapping.DbDataFileSource;
import com.emistoolbox.common.model.mapping.impl.DbDataFileSourceImpl;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/** Editor file file sources. Supports file system (with file dialog browsing) and HTTP downloads (with cache). */ 
public class MapUiFileSourceEditor extends MapUiEditorBase<DbDataFileSource> 
{
	private static String[] protocols = new String[] { DbDataFileSource.PREFIX_DATASET, DbDataFileSource.PREFIX_HTTPS, DbDataFileSource.PREFIX_HTTP, DbDataFileSource.PREFIX_MAGPI }; 

	private ListBox uiProtocol = new ListBox();  
	private TextBox uiUrl = new TextBox();  
	
	private PushButton btnBrowse = new PushButton("Browse"); 
	private PushButton btnMagpi = new PushButton("Set"); 
	private MagpiUrlEditor uiMagpi = new MagpiUrlEditor(); 
	private Label uiContextName = new Label(); 
	private TextBox uiContextValue = new TextBox(); 
	private TextBox uiCached = new TextBox(); 
	private HTML uiCachedLabel = new HTML("<small>Local filename</small>"); 
	private String[] filters; 
	
	public MapUiFileSourceEditor(EmisToolboxServiceAsync service, String dataset, String[] filters) 
	{
		super(service, dataset);
		this.filters = filters; 
		uiContextName.setStyleName("section");
		
		uiUrl.setWidth("350px");
		for (String protocol : protocols)
			uiProtocol.addItem(protocol);
		
		uiProtocol.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) 
			{
				uiUrl.setText("");
				updateUi(); 
			} 
		});

		btnBrowse.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) 
			{
				FileListDialogBox dlg = new FileListDialogBox(DbDataFileSource.PREFIX_DATASET, getService(), getDataset(), MapUiFileSourceEditor.this.filters);
				dlg.setPopupPositionAndShow(new CenteredPositionCallback(dlg));

				dlg.addValueChangeHandler(new ValueChangeHandler<String>() 
				{
					@Override
					public void onValueChange(ValueChangeEvent<String> event) 
					{
						String result = event.getValue(); 
						if (result != null && result.length() > 0 && result.startsWith("/"))
							result = result.substring(1); 
						uiUrl.setText(result);
					}
				});
			}
		}); 
		
		btnMagpi.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event)
			{
				final PopupPanel popup = new PopupPanel(); 
				
				VerticalPanel vp = new VerticalPanel(); 
				vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
				popup.setWidget(vp);
				
				PushButton btnOk = new PushButton("OK"); 
				EmisUtils.init(btnOk, 60);
				
				vp.add(uiMagpi);
				vp.add(btnOk); 
				
				btnOk.addClickHandler(new ClickHandler() 
				{
					@Override
					public void onClick(ClickEvent event) 
					{
						popup.hide(); 
						uiUrl.setText(uiMagpi.get());  
					}
				}); 
			
				popup.setModal(true);
		        popup.setAutoHideEnabled(true);
		        popup.showRelativeTo(btnMagpi);		
	        }
        }); 
		
		EmisUtils.init(btnBrowse, 60); 
		EmisUtils.init(btnMagpi, 60); 
		
		setProtocol(DbDataFileSource.PREFIX_DATASET);
		
		updateUi(); 
	}

	public void setFilters(String[] filters)
	{ this.filters = filters; } 
	
	@Override
	public void set(DbDataFileSource data) 
	{
		String url = data.getUrl();
		for (String prefix : protocols)
		{
			if (!url.startsWith(prefix))
				continue;

			setProtocol(prefix);
			uiUrl.setText(url.substring(prefix.length()));
		}

		uiContextValue.setValue(data.getContextValue());
		uiCached.setText(data.getCacheFilename());
		
		updateUi(); 
	}

	public void updateContextName(String name)
	{
		if (name == null || name.equals(""))
			uiContextName.setText(""); 
		else
			uiContextName.setText(name + " = ");
		updateUi(); 
	}
	private void updateUi()
	{
		while (getRowCount() > 0)
			removeRow(0);
		
		int row = 0; 
		getFlexCellFormatter().setColSpan(row, 1, 2);
		setWidget(row, 0, uiProtocol); 
		setWidget(row, 1, uiUrl); 
		row++; 

		if (getProtocol().startsWith(DbDataFileSource.PREFIX_MAGPI))
		{
			getCellFormatter().setHorizontalAlignment(row, 2, HasHorizontalAlignment.ALIGN_RIGHT);
			setWidget(row, 2, btnMagpi); 
			row++; 
		}
		else if (!isHttpUrl(getProtocol()))
		{
			getCellFormatter().setHorizontalAlignment(row, 2, HasHorizontalAlignment.ALIGN_RIGHT);
			setWidget(row, 2, btnBrowse); 
			row++; 
		}
		else
			getCellFormatter().setHorizontalAlignment(row, 2, HasHorizontalAlignment.ALIGN_LEFT);

		if (showContextUi())
		{
			setWidget(row, 1, uiContextName);
			setWidget(row, 2, uiContextValue);
			row++;
		}
		
		if (showCacheUi())
		{
			setWidget(row, 1, uiCachedLabel);
			setWidget(row, 2, uiCached); 
			row++; 
		}
	}
	
	private boolean showContextUi()
	{ return !uiContextName.getText().equals(""); }
	
	private boolean showCacheUi()
	{ return isHttpUrl(getProtocol()); }
	
	private String getCurrentUrl()
	{
		String protocol = getProtocol();  
		if (isHttpUrl(protocol))
		{
			String url = uiUrl.getText();
			if (url.startsWith(DbDataFileSource.PREFIX_HTTPS) || url.startsWith(DbDataFileSource.PREFIX_HTTPS) || url.startsWith(DbDataFileSource.PREFIX_MAGPI))
				return url;
			else 
				return protocol + url;
		}
		else 
			return protocol + uiUrl.getText();
	}
	
	private boolean isHttpUrl(String url)
	{ return url.startsWith(DbDataFileSource.PREFIX_HTTPS) || url.startsWith(DbDataFileSource.PREFIX_HTTP) || url.startsWith(DbDataFileSource.PREFIX_MAGPI); }
	
	@Override
	public DbDataFileSource get() 
	{
		DbDataFileSource result = new DbDataFileSourceImpl();
		result.setUrl(getCurrentUrl());
		if (showContextUi())
			result.setContextValue(uiContextValue.getText());
		if (showCacheUi())
			result.setCacheFilename(uiCached.getText()); 

		return result; 
	}
	
	private String getProtocol()
	{
		int index = uiProtocol.getSelectedIndex();
		return index == -1 ? protocols[0] : protocols[index]; 
	}
	
	private void setProtocol(String value)
	{
		for (int i = 0; i < protocols.length; i++)
		{
			if (protocols[i].equals(value))
			{
				uiProtocol.setSelectedIndex(i);
				return;
			}
		}
		
		uiProtocol.setSelectedIndex(0);
	}
}
