package com.emistoolbox.client.util.ui;

import com.emistoolbox.common.util.Named;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import java.util.List;

public class NamedSelectPopup<T extends Named> extends PopupPanel implements HasValueChangeHandlers<T>
{
    private T result = null;

    public NamedSelectPopup(List<T> items, T selectedItem) {
        this(null, items, selectedItem);
    }

    public NamedSelectPopup(String htmlInfo, List<T> items, T selectedItem) {
        setModal(true);
        setAutoHideEnabled(true);

        VerticalPanel vp = new VerticalPanel();
        vp.setSpacing(1);
        if (htmlInfo != null)
        {
            vp.add(new HTML(htmlInfo));
        }
        for (final T item : items)
        {
            Label l = new Label(item.getName());
            if ((selectedItem != null) && (item.getName().equals(selectedItem.getName())))
            {
                l.addStyleName("selected");
            }
            l.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event)
                {
                    result = item;
                    ValueChangeEvent.fire(NamedSelectPopup.this, item);
                    NamedSelectPopup.this.hide();
                }
            });
            vp.add(l);
        }

        add(vp);
    }

    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<T> handler)
    {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    public T getResult()
    {
        return this.result;
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.client.util.ui.NamedSelectPopup JD-Core
 * Version: 0.6.0
 */