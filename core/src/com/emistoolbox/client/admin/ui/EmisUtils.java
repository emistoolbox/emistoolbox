package com.emistoolbox.client.admin.ui;

import com.emistoolbox.client.Message;
import com.emistoolbox.common.renderer.pdfreport.PdfReportConfig.PageOrientation;
import com.emistoolbox.common.util.Named;
import com.emistoolbox.common.util.NamedIndexList;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CustomButton;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.ArrayList;
import java.util.List;

public class EmisUtils
{
    public static String getUniqueId(Tree tree, String prompt)
    {
        List ids = new ArrayList();
        for (int i = 0; i < tree.getItemCount(); i++)
        {
            ids.add(tree.getItem(i).getText());
        }
        return getUniqueId(ids, prompt);
    }

    public static String getUniqueId(NamedIndexList<? extends Named> list, String prompt)
    {
        List ids = new ArrayList();
        for (Named n : list)
        {
            ids.add(n.getName());
        }
        return getUniqueId(ids, prompt);
    }

    public static String getUniqueId(TreeItem parent, String prompt)
    {
        List ids = new ArrayList();
        for (int i = 0; i < parent.getChildCount(); i++)
        {
            ids.add(parent.getChild(i).getText());
        }
        return getUniqueId(ids, prompt);
    }

    public static String getUniqueId(ListBox list, String prompt)
    {
        List ids = new ArrayList();
        for (int i = 0; i < list.getItemCount(); i++)
        {
            ids.add(list.getItemText(i));
        }
        return getUniqueId(ids, prompt);
    }

    public static String getUniqueId(List<String> ids, String prompt)
    {
        String result = "";
        do
        {
            result = Window.prompt(prompt, result);
            if ((result == null) || (result.equals("")))
                return null;
        }
        while ((ids != null) && (ids.contains(result)));
        return result;
    }

    public static <T extends CustomButton> T init(T btn, int size)
    {
        btn.setStyleName("button24");
        btn.setWidth(size + "px");
        return btn;
    }

    public static <T extends CustomButton> T initSmall(T btn, int size)
    {
        btn.setStyleName("smallbutton");
        btn.setWidth(size + "px");
        return btn;
    }

    public static String getI18n(String key)
    {
        if (key.equals(PageOrientation.LANDSCAPE.toString()))
            return Message.messageAdmin().orientationLandscape();
        if (key.equals(PageOrientation.PORTRAIT.toString()))
        {
            return Message.messageAdmin().orientationPortrait();
        }
        return null;
    }

    public static void editText(String prompt, String defaultValue, ValueChangeHandler<String> handler)
    {
        TextInputDialog dlg = new TextInputDialog(prompt);
        dlg.show(defaultValue, handler);
    }

    static class TextInputDialog extends PopupPanel implements HasValueChangeHandlers<String>
    {
        private VerticalPanel vp = new VerticalPanel();
        private HorizontalPanel hp = new HorizontalPanel();
        private TextArea uiText = new TextArea();

        public TextInputDialog(String prompt) {
            vp.add(new Label(prompt));
            vp.add(uiText);

            PushButton btnOk = new PushButton("OK", new ClickHandler() {
                public void onClick(ClickEvent event)
                {
                    ValueChangeEvent.fire(TextInputDialog.this, uiText.getText());
                    hide();
                }
            });
            hp.add(btnOk);

            PushButton btnCancel = new PushButton("Cancel", new ClickHandler() {
                public void onClick(ClickEvent event)
                {
                    hide();
                }
            });
            hp.add(btnCancel);

            vp.add(hp);
        }

        public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler)
        {
            return addHandler(handler, ValueChangeEvent.getType());
        }

        public void show(String value, ValueChangeHandler<String> handler)
        {
            uiText.setText(value != null ? value : "");
            super.show();
        }
    }

    public static boolean isEmpty(String txt)
    {
        return txt == null || txt.equals("");
    }
    
    public static String getFormattedValue(String format, double value)
    {
        if (value < 0 || Double.isNaN(value))
            return "";
        
        if (format.endsWith("%"))
            return NumberFormat.getFormat(format.substring(0, format.length() - 1)).format(value) + "%";

        return NumberFormat.getFormat(format).format(value);
    }
    
    native public static void log(String message) /*-{
    	if (console)
    		console.log(message); 
    }-*/;
}
