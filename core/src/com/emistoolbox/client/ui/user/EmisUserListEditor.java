package com.emistoolbox.client.ui.user;

import java.util.ArrayList;
import java.util.List;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.client.Message;
import com.emistoolbox.client.admin.EmisToolbox;
import com.emistoolbox.client.admin.StatusAsyncCallback;
import com.emistoolbox.client.admin.ui.EmisUtils;
import com.emistoolbox.client.admin.ui.ListBoxWithUserObjects;
import com.emistoolbox.client.ui.StatusPanel;
import com.emistoolbox.common.user.EmisUser;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class EmisUserListEditor extends HorizontalPanel implements EmisEditor<List<EmisUser>>
{
    private ListBoxWithUserObjects<EmisUser> uiUsers = new ListBoxWithUserObjects<EmisUser>();
    private EmisUserEditor uiEditor = null; 
    private PushButton btnAdd = new PushButton(Message.messageAdmin().prcleAdd());
    private PushButton btnDel = new PushButton(Message.messageAdmin().prcleDel());
    private PushButton btnSave = new PushButton(Message.messageAdmin().btnSave()); 
    private EmisToolbox toolbox; 
    private String[] datasets = null; 

    public EmisUserListEditor(EmisToolbox toolbox)
    {
        this.toolbox = toolbox; 
        uiEditor = new EmisUserEditor(toolbox); 
        uiEditor.setVisible(false); 
        
        HorizontalPanel buttons = new HorizontalPanel();
        buttons.setSpacing(2);
        EmisUtils.init(this.btnAdd, 60);
        buttons.add(this.btnAdd);
        EmisUtils.init(this.btnDel, 60);
        buttons.add(this.btnDel);
        EmisUtils.init(btnSave, 60); 
        buttons.add(btnSave); 
        
        uiUsers.setWidth("200px");
        uiUsers.setVisibleItemCount(20);

        VerticalPanel vp = new VerticalPanel();
        vp.add(uiUsers);
        vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        vp.add(buttons);

        add(vp);
        add(uiEditor);
        setSpacing(5);

        setCellWidth(uiUsers, "200px");
        uiUsers.setWidth("200px");

        uiUsers.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event)
            { select(uiUsers.getSelectedIndex(), false); }
        });
        
        btnAdd.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            { addUser(); } 
        }); 

        btnDel.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            { deleteUser(); } 
        }); 
        
        btnSave.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            { 
                EmisUserListEditor.this.toolbox.setUsers(get());
                saveUsers(); 
            }
        }); 
        
        toolbox.getService().getDataSets(true, new StatusAsyncCallback<String[]>("Loading datasets") {
			@Override
			public void onSuccess(String[] result) 
			{
				super.onSuccess(result);
				uiEditor.setDatasets(result);
			}
        }); 
    }

    private void addUser()
    {
        String newId = EmisUtils.getUniqueId(uiUsers, Message.messageAdmin().prcleNewReportId());
        if (newId == null)
            return;

        EmisUser user = new EmisUser();
        user.setUsername(newId);
        uiUsers.addItem(newId, user);
        select(uiUsers.getItemCount() - 1, true);
    }

    private void saveUsers()
    { 
        final StatusPanel uiStatus = new StatusPanel(); 
        uiStatus.startProgress(); 

        toolbox.setWidget(uiStatus); 
        toolbox.getService().setUsers(get(), new StatusAsyncCallback<Void>("Saving Users") {
            public void onFailure(Throwable caught)
            {
                super.onFailure(caught);
                uiStatus.setMessage(Message.messageAdmin().usrSaveUsersTitle(), Message.messageAdmin().usrSaveUsersFailed(), false); 
            }

            public void onSuccess(Void result)
            {
                super.onSuccess(result); 
                uiStatus.setMessage(Message.messageAdmin().usrSaveUsersTitle(),  Message.messageAdmin().usrSaveUsersSuccess(), true); 
            }
        }); 
    }
    
    private void deleteUser()
    {
        int index = uiUsers.getSelectedIndex();
        if (index != -1)
            uiUsers.removeItem(index);

        index = Math.min(index, uiUsers.getItemCount() - 1);
        if (index == -1)
            show(null);
        else
            select(index, true);
    }
    
    public void commit()
    {
        EmisUser user = uiEditor.get();
        for (int i = 0; i < uiUsers.getItemCount(); i++)
        {
            if (uiUsers.getUserObject(i) == user)
                uiUsers.setItemText(i, user.getUsername()); 
        }
    }

    public List<EmisUser> get()
    {
        commit(); 
        
        List<EmisUser> result = new ArrayList<EmisUser>(); 
        for (int i = 0; i < uiUsers.getItemCount(); i++) 
            result.add(uiUsers.getUserObject(i)); 
        
        return result; 
    }

    public void set(List<EmisUser> users)
    {
        uiUsers.clear(); 
        if (users == null)
            return; 
        
        for (EmisUser user : users)
            uiUsers.addItem(user.getUsername(), user);  
                
        if (users.size() > 0)
            select(0, true); 
        else
            show(null); 
    }
    
    private void select(int index, boolean updateUi)
    {
        if (uiEditor.isVisible())
            commit();

        if (updateUi)
            uiUsers.setSelectedIndex(index);

        if (index == -1)
            show(null);
        else
            show(uiUsers.getUserObject(index));
    }
    
    private void show(EmisUser user)
    {
        if (uiEditor.isVisible())
            commit(); 

        uiEditor.set(user); 
        uiEditor.setVisible(user != null); 
    }
    
}
