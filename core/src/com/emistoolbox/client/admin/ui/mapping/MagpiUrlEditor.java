package com.emistoolbox.client.admin.ui.mapping;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.common.model.mapping.DbDataFileSource;
import com.emistoolbox.common.model.mapping.impl.DbDataFileSourceImpl;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;

public class MagpiUrlEditor extends FlexTable implements EmisEditor<String>
{
	private TextBox uiUsername = new TextBox();
	private PasswordTextBox uiPassword = new PasswordTextBox(); 
	private TextBox uiDocumentId = new TextBox(); 
	private ListBox uiFormat = new ListBox();  
	
	public MagpiUrlEditor()
	{
		int row = 0; 

		uiFormat.addItem("Excel", "xlsx");
		uiFormat.addItem("CSV", "csv");
		uiFormat.addItem("MSAccess", "mdb");
		
		setText(row, 0, "Username");
		setWidget(row, 1, uiUsername);
		row++; 

		setText(row, 0, "Password");
		setWidget(row, 1, uiPassword);
		row++; 
		
		setText(row, 0, "Document ID");
		setWidget(row, 1, uiDocumentId);
		row++; 

		setText(row, 0, "Format");
		setWidget(row, 1, uiFormat);
		row++; 
	}

	@Override
	public void commit() 
	{}

	@Override
	public String get() 
	{
		StringBuffer url = new StringBuffer(DbDataFileSource.PREFIX_MAGPI);  
		if (uiUsername.getText() != null && !uiUsername.getText().equals(""))
			url.append(uiUsername.getText()); 
		
		if (uiPassword.getText() != null && !uiPassword.getText().equals(""))
			url.append(":" + uiPassword.getText()); 
		
		if (!url.equals(DbDataFileSource.PREFIX_MAGPI))
			url.append("@"); 
		
		url.append("www.magpi.com/"); 
		url.append(uiDocumentId.getText());
		
		if (uiFormat.getSelectedIndex() != -1)
		{
			url.append("."); 
			url.append(uiFormat.getValue(uiFormat.getSelectedIndex())); 
		}

		return url.toString();
	}

	@Override
	public void set(String url) 
	{
		if (url == null || url.equals(""))
		{
			uiUsername.setText(""); 
			uiPassword.setText("");
			uiDocumentId.setText(""); 
			uiFormat.setSelectedIndex(0);
		}
		
		uiUsername.setText(DbDataFileSourceImpl.getMagpiUsername(url)); 
		uiPassword.setText(DbDataFileSourceImpl.getMagpiPassword(url));
		uiDocumentId.setText(DbDataFileSourceImpl.getMagpiDocumentId(url));
		
		String format = DbDataFileSourceImpl.getMagpiFormat(url); 
		for (int i = 0; i < uiFormat.getItemCount(); i++)
		{
			if (format.equals(uiFormat.getValue(i)))
				uiFormat.setSelectedIndex(i);
		}
	}
}
