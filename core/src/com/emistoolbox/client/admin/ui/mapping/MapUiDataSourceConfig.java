package com.emistoolbox.client.admin.ui.mapping;

import com.emistoolbox.client.EmisToolboxServiceAsync;
import com.emistoolbox.client.Message;
import com.emistoolbox.client.admin.EmisToolbox;
import com.emistoolbox.client.admin.ui.EmisUtils;
import com.emistoolbox.client.admin.ui.GwtUtils;
import com.emistoolbox.client.admin.ui.ListBoxWithUserObjects;
import com.emistoolbox.common.model.mapping.DbDataFileSource;
import com.emistoolbox.common.model.mapping.DbDataSourceConfig;
import com.emistoolbox.common.model.mapping.DbDataSourceConfigAccess;
import com.emistoolbox.common.model.mapping.DbDataSourceConfigCsv;
import com.emistoolbox.common.model.mapping.DbDataSourceConfigExcel;
import com.emistoolbox.common.model.mapping.DbDataSourceConfigFile;
import com.emistoolbox.common.model.mapping.DbDataSourceConfigGeo;
import com.emistoolbox.common.model.mapping.DbDataSourceConfigHsqldb;
import com.emistoolbox.common.model.mapping.impl.DbDataSourceConfigJdbc;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapUiDataSourceConfig extends MapUiEditorBase<DbDataSourceConfig>
{
    private ListBox uiConfigType = new ListBox();
    private DbDataSourceConfig config;
    private TextBox uiHost = new TextBox();
    private TextBox uiPort = new TextBox();
    private TextBox uiUsername = new TextBox();
    private TextBox uiPassword = new TextBox();
    private TextBox uiName = new TextBox();
    private TextBox uiContextName = new TextBox();
    private List<MapUiFileSourceEditor> uiFileSources = new ArrayList<MapUiFileSourceEditor>();
    private int newFilesRow = -1;

    private ListBoxWithUserObjects<String> uiQueries = new ListBoxWithUserObjects<String>();
    private TextArea uiQuery = new TextArea();
    private int currentQueryIndex = -1;
    private PushButton uiAddQuery = new PushButton("Add");
    private PushButton uiDelQuery = new PushButton("Del");

    public MapUiDataSourceConfig(EmisToolboxServiceAsync service, String dataset) 
    {
        super(service, dataset);

        this.uiAddQuery.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                MapUiDataSourceConfig.this.addQuery();
            }
        });
        EmisUtils.init(this.uiAddQuery, 50);

        this.uiDelQuery.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                MapUiDataSourceConfig.this.deleteQuery();
            }
        });
        EmisUtils.init(this.uiDelQuery, 50);
        this.uiQueries.setVisibleItemCount(10);
        this.uiQuery.setPixelSize(450, 200);

        this.uiConfigType.addItem("");
        for (ConfigType type : ConfigType.values())
            this.uiConfigType.addItem(type.toString());

        this.uiConfigType.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event)
            {
                if (MapUiDataSourceConfig.this.uiConfigType.getSelectedIndex() > 0)
                {
                	ConfigType configType = MapUiDataSourceConfig.ConfigType.valueOf(uiConfigType.getItemText(uiConfigType.getSelectedIndex())); 
                    setConfigType(configType, null);

                    String[] filters = getFileFilters(); 
                    for (MapUiFileSourceEditor editor : uiFileSources)
                    	editor.setFilters(filters);
                }
                else
                    MapUiDataSourceConfig.this.setConfigType(null, null);
            }
        });
        this.uiQueries.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event)
            { selectQuery(uiQueries.getSelectedIndex(), false); }
        });
        
        uiContextName.addChangeHandler(new ChangeHandler() {
        	public void onChange(ChangeEvent event)
        	{ updateContextName(uiContextName.getText()); }
        });
    }

    private void selectQuery(int index, boolean updateListBox)
    {
        if (this.currentQueryIndex != -1)
        {
            this.uiQueries.setUserObject(this.currentQueryIndex, this.uiQuery.getText());
        }
        if (updateListBox)
        {
            this.uiQueries.setSelectedIndex(index);
        }
        this.currentQueryIndex = index;
        this.uiQuery.setVisible(this.currentQueryIndex != -1);
        if (this.currentQueryIndex != -1)
            this.uiQuery.setText((String) this.uiQueries.getUserObject(this.currentQueryIndex));
    }

    private void setConfigType(ConfigType configType, DbDataSourceConfig newConfig)
    {
        clear(true);

        int row = 0;
        setHTML(row, 0, Message.messageAdmin().mapuidsDataSourceType());
        EmisToolbox.css(this, row, 0, "sectionBlue"); 
        setWidget(row, 1, this.uiConfigType);
        row++;

        GwtUtils.setListValue(this.uiConfigType, configType == null ? "" : configType.toString());

        boolean showHost = false;
        boolean showDbName = false;
        boolean showLogin = false;
        boolean showFile = false;
        boolean showQueries = false;

        if (configType != null)
        {
            if (configType == ConfigType.MSACCESS)
            {
                if (newConfig == null)
                    newConfig = new DbDataSourceConfigAccess();
                showFile = true;
                showQueries = true;
            }
            else if (configType == ConfigType.MSEXCEL)
            {
                if (newConfig == null)
                    newConfig = new DbDataSourceConfigExcel();
                showFile = true;
            }
            else if (configType == ConfigType.CSV)
            {
            	if (newConfig == null)
            		newConfig = new DbDataSourceConfigCsv(); 
            	showFile = true; 
            }
            else if (configType == ConfigType.GIS_SHAPE)
            {
                if (newConfig == null)
                    newConfig = new DbDataSourceConfigGeo();
                showFile = true;
            }
            else if (configType == ConfigType.MSSQL)
            {
                if (newConfig == null)
                {
                    newConfig = new DbDataSourceConfigJdbc();
                    ((DbDataSourceConfigJdbc) newConfig).setDriverType(DbDataSourceConfigJdbc.JdbcDriver.MSSQL);
                }

                showHost = true;
                showDbName = true;
                showLogin = true;
                showQueries = true;
            }
            else if (configType == ConfigType.MYSQL)
            {
                if (newConfig == null)
                {
                    newConfig = new DbDataSourceConfigJdbc();
                    ((DbDataSourceConfigJdbc) newConfig).setDriverType(DbDataSourceConfigJdbc.JdbcDriver.MYSQL);
                }

                showHost = true;
                showDbName = true;
                showLogin = true;
                showQueries = true;
            }
            else if (configType == ConfigType.POSTGRESQL)
            {
            	if (newConfig == null)
            	{
            		newConfig = new DbDataSourceConfigJdbc(); 
            		((DbDataSourceConfigJdbc) newConfig).setDriverType(DbDataSourceConfigJdbc.JdbcDriver.POSTGRESQL);; 
            	}
            	
            	showHost = true; 
            	showDbName = true; 
            	showLogin = true; 
            	showQueries = true; 
            }
            else if (configType == ConfigType.ODBC)
            {
                if (newConfig == null)
                {
                    newConfig = new DbDataSourceConfigJdbc();
                    ((DbDataSourceConfigJdbc) newConfig).setDriverType(DbDataSourceConfigJdbc.JdbcDriver.ODBC);
                }

                showDbName = true;
                showLogin = true;
                showQueries = true;
            }
            else if (configType == ConfigType.HSQL)
            {
            	if (newConfig == null)
            		newConfig = new DbDataSourceConfigHsqldb();
            	
            	showQueries = true; 
            	showLogin = false; 
            	showFile = true; 
            }
        }

        this.config = newConfig;

        if (showHost)
        {
            setHTML(row, 0, Message.messageAdmin().mapuidsHost());
            EmisToolbox.css(this, row, 0, "sectionBlue"); 
            HorizontalPanel hp = new HorizontalPanel();
            hp.add(this.uiHost);
            hp.add(new Label(" : "));
            this.uiPort.setWidth("20px");
            hp.add(this.uiPort);
            setWidget(row, 1, hp);
            row++;
        }

        if (showDbName)
        {
            setHTML(row, 0, Message.messageAdmin().mapuidsDbName());
            EmisToolbox.css(this, row, 0, "sectionBlue"); 
            setWidget(row, 1, this.uiName);
            row++;
        }

        if (showLogin)
        {
            setHTML(row, 0, Message.messageAdmin().mapuidsDbUsername());
            EmisToolbox.css(this, row, 0, "sectionBlue"); 
            setWidget(row, 1, this.uiUsername);
            row++;
            setHTML(row, 0, Message.messageAdmin().mapuidsDbPassword());
            EmisToolbox.css(this, row, 0, "sectionBlue"); 
            setWidget(row, 1, this.uiPassword);
            row++;
        }

        if (showFile)
        {
            setHTML(row, 0, Message.messageAdmin().mapuidsContextVariable());
            EmisToolbox.css(this, row, 0, "sectionBlue"); 
            setWidget(row, 1, this.uiContextName);
            row++;

            for (int i = 0; i < this.uiFileSources.size(); i++)
            {
                showFile(row, i, this.uiFileSources.get(i));
                row++;
            }
            if (this.uiFileSources.size() == 0)
            {
                showFile(row, 0, null);
                row++;
            }

            this.newFilesRow = row;
            HTML htmlAdd = new HTML(Message.messageAdmin().mapuidsHtmlAdd());
            htmlAdd.setStyleName("textButton");
            htmlAdd.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event)
                {
                    MapUiDataSourceConfig.this.insertRow(MapUiDataSourceConfig.this.newFilesRow);
                    MapUiDataSourceConfig.this.showFile(MapUiDataSourceConfig.this.newFilesRow, MapUiDataSourceConfig.this.uiFileSources.size(), null);
                    newFilesRow++;
                }
            });
            setWidget(row, 2, htmlAdd);
            row++;
        }

        if (showQueries)
        {
            getCellFormatter().setVerticalAlignment(row, 0, HasVerticalAlignment.ALIGN_TOP);
            setHTML(row, 0, Message.messageAdmin().mapuidsQueries() + ":");
            EmisToolbox.css(this, row, 0, "sectionBlue"); 
            getFlexCellFormatter().setColSpan(row, 1, 2);
            HorizontalPanel hp = new HorizontalPanel();
            hp.setSpacing(2);
            hp.add(this.uiQueries);
            
            VerticalPanel vp = new VerticalPanel(); 
            vp.setSpacing(3);
            vp.add(this.uiAddQuery);
            vp.add(this.uiDelQuery);
            hp.add(vp);

            setWidget(row, 1, hp);
            row++;

            getFlexCellFormatter().setColSpan(row, 1, 2);
            setWidget(row, 1, this.uiQuery);

            this.uiQuery.setVisible(false);
        }
    }

    private void showFile(int row, int count, MapUiFileSourceEditor editor)
    {
        if (editor == null)
        {
        	editor = new MapUiFileSourceEditor(getService(), getDataset(), getFileFilters()); 
            this.uiFileSources.add(editor);
        }

        setHTML(row, 0, Message.messageAdmin().mapuidsFileCount(count + 1));
        EmisToolbox.css(this, row, 0, "sectionBlue"); 
        setWidget(row, 1, editor);

        getRowFormatter().setVerticalAlign(row, HasVerticalAlignment.ALIGN_TOP);
    }

    private String[] getFileFilters()
    {
    	if (config instanceof DbDataSourceConfigAccess)
    		return new String[] { "*.mdb" }; 
    	if (config instanceof DbDataSourceConfigExcel)
    		return new String[] { "*.xls", "*.xlsx" }; 
    	if (config instanceof DbDataSourceConfigCsv)
    		return new String[] { "*.csv" }; 
    	if (config instanceof DbDataSourceConfigGeo)
    		return new String[] { "*.shp" }; 
    	
    	return new String[] { "*.*" }; 
    }
    
    public void set(DbDataSourceConfig config)
    {
        this.config = config;

        if (config == null)
            setConfigType(null, null);
        else if ((config instanceof DbDataSourceConfigJdbc))
        {
            DbDataSourceConfigJdbc jdbcConfig = (DbDataSourceConfigJdbc) config;
            this.uiName.setText(jdbcConfig.getDbName());
            this.uiHost.setText(jdbcConfig.getHost());
            this.uiPort.setText("" + jdbcConfig.getPort());
            this.uiUsername.setText(jdbcConfig.getUserId());
            this.uiPassword.setText(jdbcConfig.getPassword());

            if (DbDataSourceConfigJdbc.JdbcDriver.MSSQL == jdbcConfig.getDriverType())
                setConfigType(ConfigType.MSSQL, config);
            else if (DbDataSourceConfigJdbc.JdbcDriver.MYSQL == jdbcConfig.getDriverType())
                setConfigType(ConfigType.MYSQL, config);
            else if (DbDataSourceConfigJdbc.JdbcDriver.POSTGRESQL == jdbcConfig.getDriverType())
            	setConfigType(ConfigType.POSTGRESQL, config); 
            else if (DbDataSourceConfigJdbc.JdbcDriver.ODBC == jdbcConfig.getDriverType())
                setConfigType(ConfigType.ODBC, config);
        }
        else if ((config instanceof DbDataSourceConfigFile))
        {
            DbDataSourceConfigFile fileConfig = (DbDataSourceConfigFile) config;

            List<DbDataFileSource> fileSources = fileConfig.getFileSources(); 
            this.uiFileSources.clear();

            for (DbDataFileSource fileSource : fileSources)
            {
            	MapUiFileSourceEditor editor = new MapUiFileSourceEditor(getService(), getDataset(), getFileFilters()); 
            	editor.set(fileSource); 
            	uiFileSources.add(editor); 
            }
            
            this.uiContextName.setText(fileConfig.getContextName());
            updateContextName(fileConfig.getContextName()); 

            if ((config instanceof DbDataSourceConfigAccess))
                setConfigType(ConfigType.MSACCESS, config);
            else if ((config instanceof DbDataSourceConfigExcel))
                setConfigType(ConfigType.MSEXCEL, config);
            else if ((config instanceof DbDataSourceConfigGeo))
                setConfigType(ConfigType.GIS_SHAPE, config);
            else if (config instanceof DbDataSourceConfigCsv)
            	setConfigType(ConfigType.CSV, config); 
            else if (config instanceof DbDataSourceConfigHsqldb)
            	setConfigType(ConfigType.HSQL, config); 
        }
        if (config != null)
        {
            if (config.hasQueries())
            {
                this.uiQueries.clear();
                for (Map.Entry<String, String> entry : config.getQueries().entrySet())
                    this.uiQueries.add(entry.getKey(), entry.getValue());
            }
        }
    }

    private void updateContextName(String name)
    {
    	for (MapUiFileSourceEditor editor : uiFileSources)
    		editor.updateContextName(name);
    }

    private void deleteQuery()
    {
        int pos = this.uiQueries.getSelectedIndex();
        if (pos == -1)
        {
            return;
        }
        this.uiQueries.removeItem(pos);
        this.currentQueryIndex = -1;
        if (pos == this.uiQueries.getItemCount())
        {
            pos--;
        }
        if (pos != -1)
            selectQuery(pos, true);
    }

    private void addQuery()
    {
        String id = EmisUtils.getUniqueId(this.uiQueries, Message.messageAdmin().promptNewQueryId());
        if (id == null)
        {
            return;
        }
        this.uiQueries.add(id, "");
        selectQuery(this.uiQueries.getItemCount() - 1, true);
    }

    public DbDataSourceConfig get()
    {
        commit();
        return this.config;
    }

    public void commit()
    {
        if (this.config == null)
        {
            return;
        }
        if ((this.config instanceof DbDataSourceConfigJdbc))
        {
            DbDataSourceConfigJdbc jdbcConfig = (DbDataSourceConfigJdbc) this.config;
            jdbcConfig.setHost(this.uiHost.getText());
            jdbcConfig.setPort(getInt(this.uiPort));
            jdbcConfig.setDbName(this.uiName.getText());
            jdbcConfig.setUserId(this.uiUsername.getText());
            jdbcConfig.setPassword(this.uiPassword.getText());
        }
        else if ((this.config instanceof DbDataSourceConfigFile))
        {
            DbDataSourceConfigFile fileConfig = (DbDataSourceConfigFile) this.config;
            fileConfig.clear();
            for (MapUiFileSourceEditor editor : uiFileSources)
                fileConfig.addFileSource(editor.get());
            fileConfig.setContextName(this.uiContextName.getText());
        }

        if (this.config.hasQueries())
        {
            if (this.currentQueryIndex != -1)
            {
                this.uiQueries.setUserObject(this.currentQueryIndex, this.uiQuery.getText());
            }
            Map<String, String> queries = new HashMap<String, String>();
            for (int i = 0; i < this.uiQueries.getItemCount(); i++)
                queries.put(this.uiQueries.getItemText(i), this.uiQueries.getUserObject(i));

            this.config.setQueries(queries);
        }
    }

    private int getInt(TextBox text)
    {
        String value = text.getText();
        if ((value == null) || (value.equals("")))
            return -1;
        try
        { return Integer.parseInt(value); }
        catch (NumberFormatException ex)
        {}
        
        return -1;
    }

    private static enum ConfigType {
        MYSQL, MSSQL, POSTGRESQL, MSACCESS, HSQL, MSEXCEL, CSV, ODBC, GIS_SHAPE;
    }
}
