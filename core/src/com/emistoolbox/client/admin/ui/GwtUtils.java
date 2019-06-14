package com.emistoolbox.client.admin.ui;

import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

import java.util.ArrayList;
import java.util.List;

public class GwtUtils
{
    public static void move(TreeItem item, int offset)
    {
        if (item == null)
        {
            return;
        }
        int index = getChildIndex(item);
        if (index == -1)
        {
            return;
        }
        int size = getSiblingCount(item);

        int newIndex = index + offset;
        if ((newIndex >= size) || (newIndex < 0))
        {
            return;
        }
        Tree tree = item.getTree();
        TreeItem parent = item.getParentItem();

        removeItem(tree, parent, index);
        size--;

        List<TreeItem> tmp = new ArrayList<TreeItem>();
        while (newIndex < size)
        {
            tmp.add(removeItem(tree, parent, newIndex));
            size--;
        }

        addItem(tree, parent, item);
        for (TreeItem ti : tmp)
            addItem(tree, parent, ti);
    }

    public static TreeItem findTreeItem(Tree tree, Object userObject)
    {
        for (int i = 0; i < tree.getItemCount(); i++)
        {
            TreeItem child = tree.getItem(i);
            if (child.getUserObject() == userObject)
            {
                return child;
            }
            TreeItem result = findTreeItem(child, userObject);
            if (result != null)
            {
                return result;
            }
        }
        return null;
    }

    public static TreeItem findTreeItem(TreeItem tree, Object userObject)
    {
        for (int i = 0; i < tree.getChildCount(); i++)
        {
            TreeItem child = tree.getChild(i);
            if (child.getUserObject() == userObject)
            {
                return child;
            }
            TreeItem result = findTreeItem(child, userObject);
            if (result != null)
            {
                return result;
            }
        }
        return null;
    }

    public static TreeItem removeItem(Tree tree, TreeItem parent, int index)
    {
        TreeItem result = null;
        if (parent == null)
        {
            result = tree.getItem(index);
            tree.removeItem(result);
        }
        else
        {
            result = parent.getChild(index);
            parent.removeItem(result);
        }

        return result;
    }

    public static void addItem(Tree tree, TreeItem parent, TreeItem item)
    {
        if (parent == null)
            tree.addItem(item);
        else
            parent.addItem(item);
    }

    public static int getChildIndex(TreeItem item)
    {
        TreeItem parent = item.getParentItem();
        if (parent == null)
        {
            for (int i = 0; i < item.getTree().getItemCount(); i++)
            {
                if (item.getTree().getItem(i) == item)
                    return i;
            }
        }
        else
        {
            for (int i = 0; i < parent.getChildCount(); i++)
            {
                if (parent.getChild(i) == item)
                    return i;
            }
        }
        return -1;
    }

    public static int getSiblingCount(TreeItem item)
    {
        TreeItem parent = item.getParentItem();
        if (parent != null)
        {
            return parent.getChildCount();
        }
        return item.getTree().getItemCount();
    }

    public static String getListValue(ListBox list)
    {
        if (list.getSelectedIndex() == -1)
        {
            return null;
        }
        return list.getValue(list.getSelectedIndex());
    }

    public static void setListValueWithAdd(ListBox list, String value)
    {
        setListValueInternal(list, value, true);
    }

    public static void setListValue(ListBox list, String value)
    {
        setListValueInternal(list, value, false);
    }

    private static void setListValueInternal(ListBox list, String value, boolean withAdd)
    {
        if (value == null)
        {
            list.setSelectedIndex(-1);
            return;
        }

        for (int i = 0; i < list.getItemCount(); i++)
        {
            if (!value.equals(list.getValue(i)))
                continue;
            list.setSelectedIndex(i);
            return;
        }

        if (withAdd)
        {
            list.addItem(value);
            list.setSelectedIndex(list.getItemCount() - 1);
        }
        else
        {
            list.setSelectedIndex(-1);
        }
    }
    
    public static void setTextBox(TextBox tb, String value)
    { tb.setText(value == null ? "" : value); }
    
    public static TreeItem getTreeItem(String text)
    {
    	TreeItem result = new TreeItem(); 
    	result.setText(text);
    	return result;
    }
}

