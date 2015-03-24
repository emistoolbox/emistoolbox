package com.emistoolbox.client.admin;

import com.emistoolbox.client.EmisToolboxService;
import com.emistoolbox.client.EmisToolboxServiceAsync;
import com.emistoolbox.client.Message;
import com.emistoolbox.client.ui.user.LoginDialog;
import com.emistoolbox.client.util.ui.ListItem;
import com.emistoolbox.client.util.ui.UnorderedList;
import com.emistoolbox.common.user.EmisUser;
import com.emistoolbox.common.user.EmisUser.AccessLevel;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;

import java.util.List;

public class EmisToolbox implements EntryPoint
{
	public static final String CSS_META_RESULT_EDIT = "metaResultEdit"; 
	public static final String CSS_SELECT_LIST = "metaResultEdit"; 
	public static final String CSS_SECTION = "section"; 
	public static final String CSS_SECTION_BLUE = "sectionBlue"; 
	public static final String CSS_SUBSECTION = "subsection"; 
	public static final String CSS_VALUE = "value"; 
	public static final String CSS_BORDER = "border"; 
	
	public static final String CSS_FULL_TEXT = "fullText"; 
	public static final String CSS_HALF_TEXT = "halfText"; 
	public static final String CSS_THIRD_TEXT = "thirdText"; 
	public static final String CSS_TWO_THIRDS_TEXT = "twoThirdsText"; 
	public static final String CSS_QUATER_TEXT = "quaterText"; 

	private List<EmisUser> users; 
    private EmisUser currentUser; 
    private int currentModule = -1; 
    
    private HTML uiTitle = new HTML();
    private SimplePanel uiPanel = new SimplePanel();
    private EmisToolboxServiceAsync emisService;
    Widget[] menuItems = null;
    int selectedButton = -1;
    
    private String fixedDataset; 

    private static final int MODULE_REPORT = 1; 
    private static final int MODULE_ADMIN = 2; 
    
    public void onModuleLoad()
    {
        this.uiTitle.setStyleName("title");
        this.uiPanel.setWidth("100%");
        this.emisService = ((EmisToolboxServiceAsync) GWT.create(EmisToolboxService.class));

        prepareModule(); 
        
        // Load users before starting the software. 
        // 
        final String token = Window.Location.getParameter("token"); 
        final String ui = Window.Location.getParameter("show"); 
        if (token != null && !token.equals(""))
        {
            emisService.getAccessLevelFromToken(token, null, new StatusAsyncCallback<AccessLevel>("Verifying token") {
                public void onFailure(Throwable caught)
                {
                    super.onFailure(caught);
                    userLogin(); 
                }

                public void onSuccess(AccessLevel level)
                {
                    super.onSuccess(level);
                	
                    String[] tokenParts = token.split("-"); 
                	fixedDataset = tokenParts[2]; 
                	

                	if (level != null)
                    {
                    	currentUser = new EmisUser();
                    	currentUser.setAccessLevel(level);
                        showModule(currentModule, ui); 
                    }
                    else
                        userLogin(); 
                }
            });
        }
        else
            userLogin(); 
    }
    
    private void userLogin()
    {
        emisService.getUsers(new StatusAsyncCallback<List<EmisUser>>("Loading users") {

            public void onFailure(Throwable caught)
            { setWidget(new Label(Message.messageAdmin().usrFailedToLoad() + "\n\n" + caught.getMessage())); }

            public void onSuccess(List<EmisUser> result)
            {
                super.onSuccess(result);
                EmisToolbox.this.users = result; 

                if (users.size() > 0)
                    showLogin(currentModule == MODULE_ADMIN ? new AccessLevel[] { AccessLevel.SYSTEM_ADMIN } : null); 
                else
                    showModule(currentModule, null); 
            }
        }); 
    }

    private void showLogin(AccessLevel[] requiredLevels)
    {
        LoginDialog uiLogin = new LoginDialog(this, users, requiredLevels); 

        Grid uiGrid = new Grid(3, 3); 
        uiGrid.getCellFormatter().setHeight(0, 1, "50px"); 
        uiGrid.getCellFormatter().setWidth(0, 0, "100px"); 
        uiGrid.getCellFormatter().setWidth(0, 2, "100px"); 
        uiGrid.getCellFormatter().setHeight(2, 1, "50px"); 
        uiGrid.setWidget(1, 1, uiLogin); 
        
        setWidget(Message.messageAdmin().usrLogin(), uiGrid); 
        
        uiLogin.addValueChangeHandler(new ValueChangeHandler<EmisUser>() {
            public void onValueChange(ValueChangeEvent<EmisUser> event)
            { 
                currentUser = event.getValue();
                showModule(currentModule, null); 
            }
        }); 
    }
    
    private void showModule(int module, String ui)
    {
    	if (currentUser != null && currentUser.getDataset() != null)
    		fixedDataset = currentUser.getDataset(); 
    	
        switch (module) {
        case MODULE_ADMIN: 
            if (getAccessLevel() == AccessLevel.SYSTEM_ADMIN)
                new AdminModule(this, fixedDataset);
            else
                setWidget(new HTML(Message.messageAdmin().usrAccessDenied())); 
            break; 
            
        case MODULE_REPORT: 
            new ReportModule(this, fixedDataset == null ? "default" : fixedDataset, fixedDataset, ui); 
            break;
        }
    }

    public AccessLevel getAccessLevel()
    {
        if (users == null)
            return AccessLevel.VIEWER; 
        
        if (users.size() == 0)
            return AccessLevel.SYSTEM_ADMIN; 
        
        if (currentUser == null)
            return AccessLevel.VIEWER; 
        
        return currentUser.getAccessLevel(); 
    }
    
    private void prepareModule()
    {
        AbsolutePanel p = RootPanel.get("emis");
        if (p != null)
            currentModule = MODULE_REPORT; 
        else
            p = RootPanel.get("emisAdmin");

        if (currentModule == -1 && p != null)
            currentModule = MODULE_ADMIN; 
        
        if (currentModule == -1)
        {
            Window.alert("<div>" + Message.messageAdmin().etAlertEmisOrAdminNotFound());
            return;
        }

        p.clear();
        VerticalPanel vp = new VerticalPanel();
        vp.add(this.uiTitle);
        vp.add(this.uiPanel);
        p.add(vp);
    }

    public void setMessage(String message)
    { setWidget(new Label(message)); }

    public void setWidget(Widget w)
    {
        this.uiTitle.setVisible(false);
        this.uiPanel.setWidget(w);
    }

    public void setWidget(String title, Widget w)
    {
        this.uiTitle.setText(title);
        this.uiTitle.setVisible(true);
        this.uiPanel.setWidget(w);
    }

    public Widget getWidget()
    { return this.uiPanel.getWidget(); }

    public void setMenuItems(List<Widget> buttons)
    { setMenuItems(buttons, null); }
    
    public static Widget getMenuItem(String text, ClickHandler handler)
    {
    	Label l = new Label(text);
    	l.setStyleName("menuItem"); 
    	l.addClickHandler(handler); 
    	return l; 
    }
    
    public static void css(Widget w, String style)
    { css(w, new String[] { style }); }
    
    public static void css(Widget w, String[] styles)
    {
    	for (String style : styles)
    		w.addStyleName(style);
    }
    
    public static void css(HTMLTable table, int row, int col, String style)
    { css(table, row, col, new String[] { style }); } 
    
    public static void css(HTMLTable table, int row, int col, String[] styles)
    {
    	CellFormatter formatter = table.getCellFormatter(); 
    	for (String style : styles)
    		formatter.addStyleName(row, col, style);
    }
    
    public static String div(String style, String text)
    { return div(new String[] { style }, text); }
    
    public static String div(String[] styles, String text)
    { return tagClass("div", styles, text); } 
    
    public static String span(String style, String text)
    { return span(new String[] { style }, text); }

    public static String span(String[] styles, String text)
    { return tagClass("span", styles, text); }

    public static String tagClass(String tag, String[] styles, String text)
    {
    	StringBuffer result = new StringBuffer("<"); 
    	result.append(tag); 
    	result.append(" class=\"");
    	
    	String delim = ""; 
    	for (String style : styles)
    	{
    		result.append(delim); 
    		result.append(style);
    		delim = " "; 
    	}

    	result.append("\">"); 
    	result.append(text); 
    	result.append("</"); 
    	result.append(tag); 
    	result.append(">");  
    	
    	return result.toString(); 
    }

    public static Widget metaResultEditFrame(Widget w)
    {
    	SimplePanel result = new SimplePanel();
    	result.setWidget(w); 
    	EmisToolbox.css(result, new String[] { EmisToolbox.CSS_META_RESULT_EDIT, EmisToolbox.CSS_SELECT_LIST }); 
    	
    	return result; 
    }
    
    public void setMenuItems(List<Widget> links, Widget info)
    {
    	UnorderedList ul = new UnorderedList(); 
    	menuItems = new Widget[links.size()]; 
    	for (int i = links.size() - 1; i >= 0; i--) 
    	{
    		ListItem li = new ListItem(); 
    		li.add(links.get(i)); 
    		ul.add(li);
    		menuItems[i] = li; 
    	}
    	
        AbsolutePanel p = RootPanel.get("emisMenu");
        if (p == null)
            throw new IllegalArgumentException("<div>" + Message.messageAdmin().etErrorIdEmisMenuNotFound());
        p.clear();

        SimplePanel imagePanel = new SimplePanel(); 
        imagePanel.addStyleName("logo"); 
        imagePanel.setWidget(new Image("css/img/header2.png"));  
        p.add(imagePanel); 
        
        SimplePanel datasetPanel = new SimplePanel(); 
        datasetPanel.addStyleName("dataset"); 
        datasetPanel.setWidget(getInfoBox(info));
        p.add(datasetPanel); 
        
        SimplePanel navPanel = new SimplePanel(); 
        navPanel.addStyleName("nav"); 
        navPanel.setWidget(ul); 

        p.add(navPanel);
    }
    
    private Widget getInfoBox(Widget uiDataSet)
    {
    	// Username Widget 
        HTML uiUsername = new HTML(); 
        if (getCurrentUser() != null)
            uiUsername.setHTML(getCurrentUser().getName()); 
        uiUsername.addStyleName("pointer");
        uiUsername.addStyleName("spacing");
        uiUsername.addStyleName("value");
        uiUsername.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                if (Window.confirm(Message.messageAdmin().usrAskLogout()))
                {
                    Window.Location.reload(); 
                    return; 
                }
            }
        }); 
        
        HorizontalPanel p = new HorizontalPanel();
    	p.addStyleName("links");
    	p.addStyleName("right"); 
    	p.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    	
    	if (getCurrentUser() != null)
    	{
    		p.add(new Image("css/img/user.png")); 
        	p.add(uiUsername);
        	HTML html = new HTML("&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;");
        	html.setStyleName("spacing");
        	p.add(html); 
    	}

    	p.add(new Image("css/img/dataset.png")); 

    	HTML html = new HTML(Message.messageAdmin().infoDataSet() + ":");
    	html.setStyleName("spacing"); 
    	p.add(html); 
    	p.add(uiDataSet); 
    	
    	return p; 
    }

    public void selectButton(int index)
    {
        for (int i = 0; i < this.menuItems.length; i++)
        {
            if (i == index)
                this.menuItems[i].addStyleName("active");
            else
                this.menuItems[i].removeStyleName("active");
        }
        this.selectedButton = index;
    }

    public int getSelectedButton()
    { return this.selectedButton; }

    public EmisToolboxServiceAsync getService()
    { return this.emisService; }
    
    public List<EmisUser> getUsers()
    { return users; } 
    
    public EmisUser getCurrentUser()
    { return currentUser; } 
    
    public void setUsers(List<EmisUser> users)
    { this.users = users; } 
}
