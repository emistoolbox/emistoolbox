/*
 * Copyright 2006 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.rdf;

import info.joriki.util.Assertions;
import info.joriki.util.NotImplementedException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class RDFGraph {
  Document document;
  Element root;
  
  // the document is passed only because we need it to create elements.
  public RDFGraph (Document document,Element root) {
    this.document = document;
    this.root = root;
    Assertions.expect (root.getNodeName (),"rdf:RDF");
  }
  
  public void addAttribute (String name,String value,String namespaceURL) {
    getDescription (name,namespaceURL).setAttribute (name,value);
  }
  
  private Element getDescription (String attributeName,String namespaceURL) {
    String namespace = "xmlns:" + attributeName.substring (0,attributeName.indexOf (':')); 
    NodeList descriptions = root.getElementsByTagName ("rdf:Description");
    for (int i = 0;i < descriptions.getLength ();i++)
      if (descriptions.item (i) instanceof Element) {
        Element description = (Element) descriptions.item (i);
        if (description.getTagName ().equals ("rdf:Description") && description.hasAttribute (namespace))
          return description;
      }
    // we don't have a description element for this attribute name -- make one
    // first find the value of rdf:about
    String about = null;
    for (int i = 0;i < descriptions.getLength ();i++)
      if (descriptions.item (i) instanceof Element) {
        String thisAbout = ((Element) descriptions.item (i)).getAttribute ("rdf:about");
        if (about == null)
          about = thisAbout;
        else
          Assertions.expect (thisAbout,about);
      }
    if (about == null)
      throw new NotImplementedException ("adding description from scratch");
    Element description = document.createElement ("rdf:Description");
    description.setAttribute ("rdf:about",about);
    description.setAttribute (namespace,namespaceURL);
    root.appendChild (description);
    return description;
  }
  
  private Element getOnlyChildElement (Node node,String elementName) {
    NodeList nodeList = node.getChildNodes ();
    Element element = null;
    for (int i = 0;i < nodeList.getLength ();i++)
      if (nodeList.item (i) instanceof Element) {
        Assertions.expect (element,null);
        element = (Element) nodeList.item (i);
      }
    Assertions.expect (element.getNodeName (),elementName);
    return element;
   }
  
  public void addAttribute (String name,String value,String containerType,String itemName,String itemValue,String namespaceURL) {
    containerType = "rdf:" + containerType;
    Element description = getDescription (name,namespaceURL);
    NodeList childNodes = description.getChildNodes ();
    for (int i = 0;i < childNodes.getLength ();i++)
      if (childNodes.item (i).getNodeName ().equals (name)) {
        Element element = getOnlyChildElement (getOnlyChildElement (childNodes.item (i),containerType),"rdf:li");
        if (itemName != null)
          Assertions.expect (element.getAttribute (itemName),itemValue);
        element.setTextContent (value);
        return;
      }
    Element newElement = document.createElement (name);
    Element containerElement = document.createElement (containerType);
    Element listElement = document.createElement ("rdf:li");
    if (itemName != null)
      listElement.setAttribute (itemName,itemValue);
    listElement.setTextContent (value);
    containerElement.appendChild (listElement);
    newElement.appendChild (containerElement);
    description.appendChild (newElement);
  }
}
