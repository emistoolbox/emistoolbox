package com.emistoolbox.client.ui.results;

import java.util.List;

import com.emistoolbox.client.Message;
import com.emistoolbox.client.admin.EmisToolbox;
import com.emistoolbox.client.admin.ui.ListBoxWithUserObjects;
import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.EmisEnumTupleValue;
import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.analysis.EmisReportConfig;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaEntity.EmisGisType;
import com.emistoolbox.common.renderer.pdfreport.PdfContentConfig;
import com.emistoolbox.common.renderer.pdfreport.impl.PdfGisContentConfigImpl;
import com.emistoolbox.common.results.GisMetaResult;
import com.emistoolbox.server.EmisConfig;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.user.client.ui.ListBox;

public class GisMetaResultEditor extends MetaResultEditor<GisMetaResult>
{
    public static final String MAP_TYPE_STATIC = "static"; 
    public  static final String MAP_TYPE_INTERACTIVE = "interactive"; 
    
    private EmisMetaEntity currentPlotEntity = null;
    private String mapType = MAP_TYPE_STATIC; 
    private ListBox uiMapType = new ListBox(); 
    
    public GisMetaResultEditor(EmisToolbox toolbox, EmisMeta emisMeta, EmisReportConfig reportConfig, List<EmisEntity> rootEntities) 
    {
        super(toolbox, emisMeta, reportConfig, rootEntities);

        uiMapType.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event)
            {
                // Map type UI is only shown if we can display the map again - change value and fire again. 
                if (uiMapType.getSelectedIndex() == -1)
                {
                    mapType = MAP_TYPE_STATIC;
                    uiMapType.setSelectedIndex(0); 
                }
                else
                    mapType = uiMapType.getValue(uiMapType.getSelectedIndex()); 
                
                ValueChangeEvent.fire(GisMetaResultEditor.this, get()); 
            }
        }); 
        updateUi();
    }

    protected void updateUi()
    {
        clear(true);
        
        if (showCurrentHierarchy(0))
            return;

        if (showCurrentPlot(2))
            return;

        if (showCurrentMetaResultValues(4, this.currentPlotEntity, Message.messageAdmin().mreHtmlIndicator(), 1, true))
            return;

        if (showEntityEditor(6))
            return;

        if (showDateEditor(8, getUsedDateTypes()))
            return;

        showMapType(10);
        showAddToReport(this, 12, 0, new String[] { Message.messageAdmin().enumEditBtnAdd() });
        ValueChangeEvent.fire(this, get());
    }

    private void showMapType(int row)
    {
        uiMapType.clear();
        
        EmisEntity entity = getCurrentEntity();
        boolean allowInteractive = (entity == null || entity.getEntityType().getGisType() != EmisGisType.COORDINATE) && EmisConfig.ADVANCED_MAPS; 
        
        uiMapType.addItem("Static Map", MAP_TYPE_STATIC); 
        if (allowInteractive)
            uiMapType.addItem("Interactive Map", MAP_TYPE_INTERACTIVE); 
        else
            mapType = MAP_TYPE_STATIC; 
        
        uiMapType.setSelectedIndex(mapType.equals(MAP_TYPE_STATIC) ? 0 : 1); 
        
        setSectionHTML(row, 0, "Map Type");
        setWidget(row + 1, 0, uiMapType); 
    }
    
    private boolean showCurrentPlot(final int row)
    {
        setSectionHTML(row, 0, Message.messageAdmin().mreHtmlPlotEntity());
        if (this.currentPlotEntity == null)
        {
            editPlotEntity(row);
            return true;
        }

        show(row + 1, this.currentPlotEntity, new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                GisMetaResultEditor.this.editPlotEntity(row);
            }
        });
        return false;
    }

    @Override
    protected void configureEntityEditor(IHierarchyBrowser editor)
    {
        super.configureEntityEditor(editor);
        editor.setEntityType(currentPlotEntity);
    }

    private void editPlotEntity(int row)
    {
        final ListBoxWithUserObjects<EmisMetaEntity> uiEntities = new ListBoxWithUserObjects();
        uiEntities.add("", null);
        for (EmisMetaEntity entity : getCurrentHierarchy().getEntityOrder())
        {
            if (entity.getGisType() == EmisMetaEntity.EmisGisType.NONE)
            {
                continue;
            }
            uiEntities.add(entity.getName(), entity);
        }

        uiEntities.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event)
            {
                currentPlotEntity = uiEntities.getValue();
                updateUi();
            }
        });
        setWidget(row + 1, 0, uiEntities);
        removeFromRow(row + 2);
    }

    protected EmisEnumTupleValue getDefaultDate()
    {
        return null;
    }

    public void commit()
    {
        super.commit();
        EmisContext context = getContext(true, true);
        context.setEntityType(this.currentPlotEntity);
        ((GisMetaResult) this.metaResult).setContext(context);
    }

    protected PdfContentConfig getContentConfig(int addButton)
    {
        PdfGisContentConfigImpl result = new PdfGisContentConfigImpl();
        result.setMetaResult(get().createCopy());
        return result;
    }
    
    public String getMapType()
    { return mapType; } 
}

