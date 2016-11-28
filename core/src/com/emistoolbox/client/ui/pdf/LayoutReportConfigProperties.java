package com.emistoolbox.client.ui.pdf;

import com.emistoolbox.client.admin.ui.ListBoxWithUserObjects;
import com.emistoolbox.client.util.ui.UIUtils;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig;
import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig.PageOrientation;
import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig.PageSize;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

public class LayoutReportConfigProperties extends LayoutProperties<EmisPdfReportConfig>
{
	private EmisPdfReportConfig config = null; 
	
	private ListBox uiPageSize = new ListBox();  
	private ListBox uiPageOrientation = new ListBox(); 
    private ListBoxWithUserObjects<EmisMetaEntity> uiEntity = new ListBoxWithUserObjects<EmisMetaEntity>();
	private TextBox uiName = new TextBox(); 
    
    private EmisMeta meta; 
    
//	private Map<String, TextSetEntryUi> uiTexts; 

	
	public LayoutReportConfigProperties(EmisMeta emis)
	{
		super(null); 
		
		this.meta = emis; 
		
		uiPageSize.addChangeHandler(getChangeHandler()); 
		UIUtils.listBoxInit(uiPageSize, PageSize.values()); 

		uiPageOrientation.addChangeHandler(getChangeHandler()); 
		UIUtils.listBoxInit(uiPageOrientation, PageOrientation.values());

		int row = getRowCount(); 

		setText(row, 0, "Report"); 
		setWidget(row++, 1, uiName); 
		
		setText(row, 0, "Page Size");
		setWidget(row++, 1, uiPageSize); 
		
		setText(row, 0, "Page Orientation"); 
		setWidget(row++, 1, uiPageOrientation);
		
		setText(row, 0, "Location"); 
		setWidget(row++, 1, uiEntity); 
        for (EmisMetaEntity entity : emis.getEntities())
            this.uiEntity.addItem(entity.getName(), entity);

		uiPageSize.addChangeHandler(getChangeHandler()); 
		uiPageOrientation.addChangeHandler(getChangeHandler()); 
		
//		uiTexts = initTexts(LayoutPageConfig.TEXT_KEYS, new String[] { "Default Title", "Default Subtitle", "Default Footer"}); 
		set(null); 
	}
	
	@Override
	public void commit() 
	{
		if (config == null)
			return; 
	
//		updateTextSet(uiTexts, config);
		config.setPage(PageSize.valueOf(UIUtils.getListBoxValue(uiPageSize)), PageOrientation.valueOf(UIUtils.getListBoxValue(uiPageOrientation)));
		config.setEntityType(uiEntity.getUserObject());
		config.setName(uiName.getText());
	}

	@Override
	public EmisPdfReportConfig get() 
	{
		commit(); 
		return config;
	}

	@Override
	public void set(EmisPdfReportConfig config) 
	{
		this.config = config; 
		if (config == null)
			setVisible(false); 
		else
		{
			setVisible(true); 

			uiName.setText(config.getName());
			UIUtils.setListBoxValue(uiPageSize, config.getPageSize().toString());
			UIUtils.setListBoxValue(uiPageOrientation, config.getOrientation().toString());

	        EmisMetaEntity reportEntity = config.getJuniorEntity();
	        EmisMetaHierarchy hierarchy = config.getHierarchy();

	        // Update report location level UI. 
	        uiEntity.clear();
	        for (EmisMetaEntity entity : this.meta.getEntities())
	            if ((reportEntity == null) || (hierarchy == null) || (!reportEntity.isChildOf(entity, hierarchy)))
	                this.uiEntity.addItem(entity.getName(), entity);
	        
	        this.uiEntity.setValue(config.getEntityType());
		}
	}
}
