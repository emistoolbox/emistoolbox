package com.emistoolbox.client.ui.pdf;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfReportConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPdfReportConfig;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class EmisPdfReportEditor extends SimplePanel implements EmisEditor<EmisPdfReportConfig> 
{
	private LayoutPdfReportEditor layoutEditor; 
	private PdfReportEditor simpleEditor;
	private EmisEditor<EmisPdfReportConfig> currentEditor; 
	
	public EmisPdfReportEditor(EmisMeta emis)
	{
		layoutEditor = new LayoutPdfReportEditor(); 
		simpleEditor = new PdfReportEditor(emis);
	}
	
	@Override
	public void commit() 
	{
		if (currentEditor != null)
			currentEditor.commit();
	}	

	@Override
	public EmisPdfReportConfig get() 
	{
		if (currentEditor == null)
			return null; 
		
		return currentEditor.get(); 
	}

	@Override
	public void set(EmisPdfReportConfig config) 
	{
		if (config == null)
			setEditor(null, null);
		else if (config instanceof PdfReportConfig)
			setEditor(simpleEditor, config); 
		else if (config instanceof LayoutPdfReportConfig)
			setEditor(layoutEditor, config); 
	}
	
	private void setEditor(EmisEditor editor, EmisPdfReportConfig config)
	{
		setWidget((Widget) editor); 
		currentEditor = editor; 
		if (editor == null)
			return; 
		
		editor.set(config);
	}
}
