package com.emistoolbox.client.ui;

import com.emistoolbox.client.admin.ui.EmisUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.rpc.server.Pair;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class TextEditor extends VerticalPanel implements HasValueChangeHandlers<String>
{
    private TextBox uiTitle = new TextBox(); 
    private TextArea uiTextArea = new TextArea();
    private PushButton btnOk = new PushButton("OK"); 
    private PushButton btnCancel = new PushButton("Cancel");
    
    public TextEditor(String title, final String defaultText)
    { this(title, null, defaultText); }

    public TextEditor(String title, final String defaultTitle, final String defaultText)
    {
        setSpacing(4); 
        add(new Label(title)); 
        setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER); 
        
        if (defaultTitle != null)
        {
            uiTitle.setText(defaultTitle); 
            uiTitle.setWidth("380px"); 
            add(uiTitle); 
        }
        
        uiTextArea.setText(defaultText); 
        uiTextArea.setSize("380px", "200px"); 
        add(uiTextArea); 
        
        HorizontalPanel hp = new HorizontalPanel();
        hp.add(EmisUtils.init(btnOk, 80)); 
        hp.add(EmisUtils.init(btnCancel, 80)); 
        add(hp); 

        btnOk.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            { ValueChangeEvent.fire(TextEditor.this, uiTextArea.getText()); }
        }); 

        btnCancel.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            { ValueChangeEvent.fire(TextEditor.this, defaultText); }
        }); 
    }

    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler)
    { return addHandler(handler, ValueChangeEvent.getType()); }
    
    public String getText()
    { return uiTextArea.getText(); } 
    
    public String getTitle()
    { return uiTitle.getText(); } 
}
