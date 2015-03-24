package com.emistoolbox.client.ui.user;

import java.util.List;

import com.emistoolbox.client.Message;
import com.emistoolbox.client.admin.EmisToolbox;
import com.emistoolbox.client.admin.StatusAsyncCallback;
import com.emistoolbox.client.admin.ui.EmisUtils;
import com.emistoolbox.common.user.EmisUser;
import com.emistoolbox.common.user.EmisUser.AccessLevel;
import com.emistoolbox.common.util.NamedUtil;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class LoginDialog extends VerticalPanel implements HasValueChangeHandlers<EmisUser>
{
    private List<EmisUser> users; 
    
    private TextBox uiUsername = new TextBox();  
    private HTML uiError = new HTML();
    private PasswordTextBox uiPassword = new PasswordTextBox();
    private PushButton btnLogon = new PushButton(Message.messageAdmin().usrLogin()); 
    
    public LoginDialog(final EmisToolbox toolbox, List<EmisUser> users, final AccessLevel[] requiredLevels) 
    {
        this.users = users; 
        
        uiError.setWidth("300px"); 
        add(new HTML("<b>" + Message.messageAdmin().usrUsername() + ":</b>")); 
        add(uiUsername);
        add(uiError); 
        uiError.setVisible(false); 
        add(new HTML("<br><b>" + Message.messageAdmin().usrPassword() + ":</b>")); 
        add(uiPassword);
        
        setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT); 
        EmisUtils.init(btnLogon, 150); 
        add(new HTML("&nbsp")); 
        setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER); 
        add(btnLogon); 
        btnLogon.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            { 
                final EmisUser user = NamedUtil.find(uiUsername.getText(), LoginDialog.this.users);
                if (user == null)
                    showFailedLogin(Message.messageAdmin().usrLoginFailed()); 
                else
                {
                    if (user.getPassword() != null && user.getPassword().equals(uiPassword.getText()))
                    { 
                        ValueChangeEvent.fire(LoginDialog.this, user); 
                        return; 
                    }
                    
                    toolbox.getService().getPasswordHash(uiPassword.getText(), new StatusAsyncCallback<String>("Encrypt password.") {
                        public void onFailure(Throwable caught)
                        {
                            super.onFailure(caught); 
                            showFailedLogin(Message.messageAdmin().usrLoginFailed()); 
                        }

                        public void onSuccess(String result)
                        {
                            super.onSuccess(result);
                            if (!user.getPasswordHash().equals(result))
                                showFailedLogin(Message.messageAdmin().usrLoginFailed()); 
                            else if (hasRequiredLevel(user, requiredLevels))
                                ValueChangeEvent.fire(LoginDialog.this, user);
                            else showFailedLogin(Message.messageAdmin().usrAccessDenied()); 
                        }
                    }); 
                }
            }
        }); 
    }
    
    private boolean hasRequiredLevel(EmisUser user, AccessLevel[] requiredLevels)
    {
        if (requiredLevels == null)
            return true; 
        
        if (user.getAccessLevel() == null)
            user.setAccessLevel(AccessLevel.VIEWER);  
        
        for (AccessLevel level : requiredLevels)
            if (level == user.getAccessLevel())
                return true; 
                
       return false; 
    }
    
    
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<EmisUser> handler)
    { return addHandler(handler,  ValueChangeEvent.getType()); }

    private void showFailedLogin(String message)
    {
        uiError.setHTML("<font color='red'>" + message + "</font>"); 
        uiError.setVisible(true); 
    }
}
