package com.emistoolbox.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.List;

public class DropDownSelect extends SimplePanel implements HasValueChangeHandlers<Integer>
{
	private HTML currentHtml = new HTML(); 
	
    private List<String> items = new ArrayList<String>();
    private List<String> values = new ArrayList<String>();

    private int selectedIndex = -1;
    private ClickHandler clickHandler; 

    public DropDownSelect() {
        addStyleName("pointer");

        clickHandler = new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                final PopupPanel popup = new PopupPanel();
                popup.setAutoHideEnabled(true);
                popup.setModal(true);

                VerticalPanel vp = new VerticalPanel();
                popup.setWidget(vp);

                for (int i = 0; i < DropDownSelect.this.items.size(); i++)
                {
                    final int index = i;
                    HTML w = new HTML((String) DropDownSelect.this.items.get(i));
                    w.addClickHandler(new ClickHandler() {
                        public void onClick(ClickEvent event)
                        {
                            DropDownSelect.this.setSelectedIndex(index);
                            popup.hide();
                            ValueChangeEvent.fire(DropDownSelect.this, Integer.valueOf(index));
                        }
                    });
                    vp.add(w);
                }

                popup.showRelativeTo(DropDownSelect.this);
            }
        };

        currentHtml.addClickHandler(clickHandler); 
        setWidget(currentHtml); 
    }

    public ClickHandler getClickHandler()
    { return clickHandler; } 

    public int getSelectedIndex()
    {
        return this.selectedIndex;
    }

    public void setSelectedValue(String value)
    {
        if (value == null)
        {
            setSelectedIndex(-1); 
            return; 
        }
        
        for (int i = 0; i < values.size(); i++) 
        {
            if (value.equals(values.get(i)))
            {
                setSelectedIndex(i); 
                return; 
            }
        }
        
        setSelectedIndex(-1); 
    }
    
    public void setSelectedIndex(int index)
    {
        this.selectedIndex = index;
        updateUi();
    }

    public void addItem(String html)
    {
        addItem(html, "");
    }

    public void addItem(String html, String value)
    {
        this.items.add(html);
        this.values.add(value);
    }

    public void updateUi()
    {
        String html = this.selectedIndex == -1 ? "-" : (String) this.items.get(this.selectedIndex);
        currentHtml.setHTML("<b>" + html + "</b>");
    }

    public void clear()
    {
        this.selectedIndex = -1;
        this.items.clear();
        this.values.clear();
    }

    public String getValue(int i)
    {
        return (String) this.values.get(i);
    }

    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Integer> handler)
    {
        return addHandler(handler, ValueChangeEvent.getType());
    }
}


