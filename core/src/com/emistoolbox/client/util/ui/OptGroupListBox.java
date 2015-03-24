package com.emistoolbox.client.util.ui;

import java.util.ArrayList;
import java.util.List;

import com.emistoolbox.client.admin.ui.EmisUtils;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList; 
import com.google.gwt.dom.client.OptGroupElement;
import com.google.gwt.dom.client.OptionElement;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.user.client.ui.ListBox;

/**
 * Extends {@code ListBox} so it can use the {@code <optgroup>} -element. Please
 * use only {@code addGroup()} and {@code addGroupItem()}. It is not necessary
 * to call {@code addGroup()} before calling {@code addGroupItem()}.
 * 
 * @author denny.kluge@gmail.com
 * 
 */
public class OptGroupListBox extends ListBox 
{
	private static final String TAG_OPTGROUP = "optgroup"; 
	private static final String TAG_OPTION = "option"; 
	private static final String TAG_SELECT = "select"; 
	
	private OptGroupElement latestOptGroupElement;

	/**
	 * Creates a new group within the ListBox.
	 * 
	 * @param groupName
	 *           the name for the group
	 */
	public void addGroup(String groupName) 
	{ this.latestOptGroupElement = createGroup(groupName); }
	
	private OptGroupElement createGroup(String groupName)
	{
		OptGroupElement groupElement = Document.get().createOptGroupElement();
		groupElement.setLabel(groupName);
		SelectElement select = this.getElement().cast();
		select.appendChild(groupElement);
		
		return groupElement; 
	}
		
	private OptGroupElement findOptGroupElement(String groupName)
	{
		SelectElement select = this.getElement().cast();
		NodeList<Node> children = select.getChildNodes(); 
		for (int i = 0; i < children.getLength(); i++)
		{
			if (hasTagName(children.getItem(i), TAG_OPTGROUP))
			{
				OptGroupElement optGroup = (OptGroupElement) children.getItem(i); 
				if (sameName(groupName, optGroup.getLabel()))
					return optGroup; 
			}
		}
		
		return null; 
	}
	
	public void resetGroup()
	{ this.latestOptGroupElement = null; } 

	/**
	 * Adds a new group item. It is added within the last created group; if there
	 * was no group created before, the item has no group membership. This method
	 * can be used if key and value are the same.
	 * 
	 * @param keyLabelName
	 *           key and label for the item
	 */
	public int addGroupItem(String keyLabelName) 
	{ return addGroupItemInternal(latestOptGroupElement, keyLabelName, keyLabelName); }
	
    public int addGroupItem(String groupName, String key, String name)
    {
    	Element parentTag = null; 
    	if (groupName != null && !groupName.isEmpty())
    	{
	    	parentTag = findOptGroupElement(groupName);
	    	if (parentTag == null)
	    		parentTag = createGroup(groupName); 
    	}
    	
    	return addGroupItemInternal(parentTag, key, name);  
    }
    
	private int addGroupItemInternal(Element parentTag, String key, String label)
	{
		if (parentTag == null)
			parentTag = getElement(); 

		OptionElement optTag = Document.get().createOptionElement();
		optTag.setInnerText(label);
		optTag.setValue(key);

		if (hasTagName(parentTag, TAG_SELECT))
			insertOption((SelectElement) parentTag, optTag); 
		else
			parentTag.appendChild(optTag);

		return findIndex(optTag); 
	}
	
	private void insertOption(SelectElement parentTag, OptionElement optTag)
	{
		NodeList<Node> nodes = parentTag.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) 
			if (hasTagName(nodes.getItem(i), TAG_OPTGROUP))
			{
				parentTag.insertBefore(optTag, nodes.getItem(i)); 
				return; 
			}
		
		parentTag.appendChild(optTag); 
	}
	
	private int findIndex(Element child, Element parent)
	{
		NodeList<Node> nodes = parent.getChildNodes(); 
		for (int i = 0; i < nodes.getLength(); i++)
		{
			Node n = nodes.getItem(i);
			if (n == child)
				return i; 
		}
		
		return -1; 
	}
	
	private int findIndex(OptionElement optTag)
	{
		List<OptionElement> options = getOptions();   
		for (int i = 0; i < options.size(); i++)
		{
			OptionElement tmp = options.get(i); 
			if (sameOptionElement(tmp, optTag))
				return i; 
		}
		
		return -1; 
	}
	
	public List<OptionElement> getOptions()
	{
		SelectElement selectCtrl = getElement().cast(); 

		List<OptionElement> result = new ArrayList<OptionElement>(); 
		NodeList<Node> nodes = selectCtrl.getChildNodes(); 
		for (int i = 0; i < nodes.getLength(); i++)
		{
			Node node = nodes.getItem(i); 
			if (hasTagName(node, TAG_OPTION))
				result.add((OptionElement) node);
			else if (hasTagName(node, TAG_OPTGROUP))
			{
				NodeList<Node> childNodes = node.getChildNodes();
				for (int j = 0; j < childNodes.getLength(); j++)
				{
					Node childNode = childNodes.getItem(j); 
					if (hasTagName(childNode, TAG_OPTION))
						result.add((OptionElement) childNode);
				}
			}
		}
		
		return result; 
	}
	
	private boolean sameOptionElement(OptionElement opt1, OptionElement opt2)
	{ return opt1 == opt2; }

    protected boolean move(int offset)
    {
    	if (getSelectedIndex() == -1)
    		return false; 
    	
    	if (offset == 0)
    		return true; 

    	SelectElement ctrl = getElement().cast(); 
    	OptionElement optTag = getOptions().get(getSelectedIndex()); 
    	Element parentTag = optTag.getParentElement();

    	int localIndex = findIndex(optTag, parentTag); 
    	int newIndex = localIndex + offset; 
    	int optCount = getOptionCount(parentTag); 
    	if (newIndex < 0 || newIndex >= optCount)
    		return false; 

    	parentTag.removeChild(optTag); 
    	if (newIndex < optCount && optCount < parentTag.getChildNodes().getLength())
        	parentTag.insertBefore(optTag, parentTag.getChildNodes().getItem(newIndex)); 
    	else 
        	parentTag.appendChild(optTag); 
    	
    	return true; 
    }
    
    private int getOptionCount(Element parentTag)
    {
    	int result = 0; 
    	NodeList<Node> nodes = parentTag.getChildNodes(); 
    	for (int i = 0; i < nodes.getLength(); i++)
    	{
    		if (hasTagName(nodes.getItem(i), TAG_OPTION))
    			result++; 
    	}; 
    	
    	return result; 
    }
    	
	protected boolean sameName(String name1, String name2)
	{
		if (name1 != null && name1.isEmpty())
			name1 = null; 
		
		if (name2 != null && name2.isEmpty())
			name2 = null; 
		
		if (name1 == null)
			return name2 == null; 
		
		return name1.equals(name2); 
	}
	
	protected String getGroupName()
	{
		if (getSelectedIndex() == -1)
			return null; 

		return getGroupName(getSelectedIndex()); 
	}
	
    public String getGroupName(int index)
    {
    	SelectElement selectElement = this.getElement().cast(); 
    	return getGroupName(getOptions().get(index));   
    }
    
	protected String getGroupName(OptionElement optElement)
	{
		if (hasTagName(optElement.getParentElement(), TAG_OPTGROUP))
			return ((OptGroupElement) optElement.getParentElement()).getLabel(); 
		else
			return null; 
	}
	
	public void removeEmptyGroups()
	{
		SelectElement tag = getElement().cast();
		NodeList<Node> nodes = tag.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++)
		{
			Node child = nodes.getItem(i); 
			if (!hasTagName(child, TAG_OPTGROUP))
				continue; 
			
			if (child.getChildNodes().getLength() == 0)
				tag.removeChild(child); 
		}
	}
	
	private static boolean hasTagName(Node node, String tagName)
	{
		if (node == null || node.getNodeName() == null)
			return false; 
		
		return tagName.equalsIgnoreCase(node.getNodeName()); 
	}
	
    public void removeItem(int index)
    { removeIndex(getElement(), index); } 
    
    private int removeIndex(Node parentNode, int index)
    {
    	NodeList<Node> nodes = parentNode.getChildNodes();
    	for (int i = 0; i < nodes.getLength(); i++)
    	{
    		Node node = nodes.getItem(i);
    		if (hasTagName(node, TAG_OPTION))
    		{
    			if (index == 0)
    			{
    				parentNode.removeChild(node); 
    				return -1; 
    			}
    		}
    		else if (hasTagName(node, TAG_OPTGROUP))
    			index = removeIndex(node, index); 
    		
    		index--; 
    	}
    	
    	if (index < 0)
    		return -1; 
    	
    	return index; 
    }
}
