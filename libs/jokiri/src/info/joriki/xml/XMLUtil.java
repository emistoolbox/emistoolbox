/*
 * Copyright 2006 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.xml;

import info.joriki.io.Streamable;
import info.joriki.util.ThreadLocalCount;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XMLUtil {
  private XMLUtil () {}
  
  final private static Transformer transformer;
  final private static DocumentBuilder documentBuilder;
  final private static DOMImplementation implementation;
  
  static {
    try {
      transformer = TransformerFactory.newInstance ().newTransformer ();
    } catch (TransformerConfigurationException e) {
      e.printStackTrace ();
      throw new Error ("couldn't create XML transformer");
    } catch (TransformerFactoryConfigurationError e) {
      e.printStackTrace ();
      throw new Error ("couldn't create XML transformer");
    }
    try {
      documentBuilder = DocumentBuilderFactory.newInstance ().newDocumentBuilder ();
    } catch (ParserConfigurationException pce) {
      pce.printStackTrace ();
      throw new Error ("couldn't create XML document builder");
    }
    implementation = documentBuilder.getDOMImplementation ();
  }

  public static Document createDocument (String namespace,String tagName) {
    return implementation.createDocument (namespace,tagName,null);
  }
  
  public static Document getDocument (File file) {
    return getDocument (new StreamSource (file));
  }
  
  public static Document getDocument (InputStream in) {
    return getDocument (new StreamSource (in));
  }
  
  public static Document getDocument (Reader reader) {
    return getDocument (new StreamSource (reader));
  }
  
  private static Document getDocument (StreamSource source) {
    Document document = documentBuilder.newDocument ();
    try {
      transformer.transform (source,new DOMResult (document));
    } catch (TransformerException te) {
      te.printStackTrace ();
      throw new Error ("couldn't transform XML document");
    }
    return document;
  }

  public static void write (Document document,File file) {
    write (document,new StreamResult (file));
  }

  public static void write (Document document,OutputStream out) {
    write (document,new StreamResult (out));
  }

  public static void write (Document document,Writer writer) {
    write (document,new StreamResult (writer));
  }

  private static void write (Document document,StreamResult result) {
    try {
      transformer.transform (new DOMSource (document),result);
    } catch (TransformerException te) {
      te.printStackTrace ();
      throw new Error ("couldn't transform XML document");
    }
  }
  
  public static Streamable getStreamable (final Document document) {
    return new Streamable () {
      public void writeTo (OutputStream out) {
        write (document,out);
      }
    };
  }
  
  private static ThreadLocalCount idCount = new ThreadLocalCount ();
  public static String getId (Element element) {
    String id = element.getAttribute ("id");
    // TODO: change to isEmpty () after upgrade to JDK 1.6
    if (id.length () == 0) {
      id = "id" + idCount.nextCount ();
      element.setAttribute ("id",id);
    }
    return id;
  }
}
