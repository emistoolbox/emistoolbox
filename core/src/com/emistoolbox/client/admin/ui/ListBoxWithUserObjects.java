package com.emistoolbox.client.admin.ui;

import com.emistoolbox.client.util.ui.OptGroupListBox;
import com.emistoolbox.common.util.Named;
import com.emistoolbox.common.util.NamedUtil;
import com.google.gwt.dom.client.OptionElement;

import java.util.ArrayList;
import java.util.List;

public class ListBoxWithUserObjects<T> extends OptGroupListBox
{
    private List<T> userObjects = new ArrayList<T>();

    public void add(T userObject)
    {
        if ((userObject instanceof Named))
            add(((Named) userObject).getName(), userObject);
    }

    public int add(String item, T userObject)
    {
    	int index = addGroupItem(item); 
    	this.userObjects.add(index, userObject); 
    	return index; 
    }
    
    public int add(String groupName, String item, T userObject)
    {
    	int index = addGroupItem(groupName, item, item); 
    	this.userObjects.add(index, userObject); 

    	return index; 
    }
    
    public void updateText(int i, String title)
    { 
    	OptionElement opt = getOptions().get(i); 
    	opt.setText(title);
    }
    
    public T getUserObject(int index)
    { return this.userObjects.get(index); }

    public void setUserObject(int index, T userObject)
    { this.userObjects.set(index, userObject); }

    public T getUserObject()
    {
        if (userObjects.size() == 0 || getSelectedIndex() == -1)
            return null;

        return getUserObject(getSelectedIndex());
    }
    
    public int getUserObjectCount()
    { return userObjects.size(); } 

    public void setValue(T value)
    {
        boolean validateWithName = value instanceof Named;

        for (int i = 0; i < this.userObjects.size(); i++)
        {
            if (((!validateWithName) || (!NamedUtil.sameName((Named) this.userObjects.get(i), (Named) value))) && ((validateWithName) || (this.userObjects.get(i) != value)))
                continue;
            try
            { setSelectedIndex(i); }
            catch (Throwable err)
            {}
            
            return;
        }
        try
        { setSelectedIndex(-1); }
        catch (Throwable err)
        {}
    }

    public String getItemText()
    {
        int index = getSelectedIndex();
        if (index == -1)
            return null;
        
        OptionElement opt = getOptions().get(index);
        return opt.getText(); 
    }

    public T getValue()
    {
        int index = getSelectedIndex();
        if (index == -1)
            return null;

        return this.userObjects.get(index);
    }

    public int addItem(String name, T userObject)
    {
        int index = addGroupItem(name);
        this.userObjects.add(index, userObject);
        return index; 
    }

    public void removeItem(int index)
    {
        super.removeItem(index);
        this.userObjects.remove(index);
    }

//    public void insertItem(int index, String name, T userObject)
//    {
//    	add(userObject);
//        super.insertItem(name, index);
//        this.userObjects.add(index, userObject);
//    }

    public void clear()
    {
        super.clear();
        this.userObjects.clear();
    }
    
    protected boolean move(int offset)
    {
    	int index = getSelectedIndex();
    	
    	if (!super.move(offset))
    		return false; 
    	

    	T item = userObjects.remove(index);   	
//    	if (index + offset == userObjects.size())
//    		userObjects.add(item); 
//    	else
    		userObjects.add(index + offset, item); 
    	
    	return true; 
    }
    
    public void moveUp()
    { move(-1); }
    
    public void moveDown()
    { move(1); }
     
    public int findIndex(T userObject)
    { return userObjects.indexOf(userObject); }
    
    public int update(String groupName, String name, T userObject)
    {
    	if (groupName == null || groupName.isEmpty())
    		groupName = null; 
    	
    	int index = findIndex(userObject); 
    	if (index == -1)
    		return index; 
    	
    	String currentGroupName = getGroupName(index);
    	if (!sameName(groupName, currentGroupName))
    	{
    		removeItem(index);
    		int result = add(groupName, name, userObject);
    		removeEmptyGroups();
    		return result; 
    	}
    	
    	updateText(index, name);
    	return index; 
    }
}

