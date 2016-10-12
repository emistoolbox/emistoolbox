/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.io.IOException;

import java.awt.Container;
import java.awt.GridLayout;

import java.util.List;
import java.util.ArrayList;

import info.joriki.util.General;
import info.joriki.util.Handler;

import info.joriki.graphics.Rectangle;

public class PDFArray extends PDFContainer
{
  List<PDFObject> contents;

  public PDFArray ()
  {
    this (new ArrayList<PDFObject> ());
  }

  public PDFArray (int n)
  {
    this (General.nullList (n));
  }

  PDFArray (List<PDFObject> contents)
  {
    this.contents = contents;
  }
  
  public PDFArray (int [] arr)
  {
    this ();
    for (int i = 0;i < arr.length;i++)
      add (new PDFInteger (arr [i]));
  }

  public PDFArray (float [] arr)
  {
    this ();
    for (int i = 0;i < arr.length;i++)
      add (new PDFReal (arr [i]));
  }

  public PDFArray (double [] arr)
  {
    this ();
    for (int i = 0;i < arr.length;i++)
      add (new PDFReal (arr [i]));
  }

  public PDFArray (Rectangle rect)
  {
    this (rect.toDoubleArray ());
  }

  public PDFArray (PDFObject ... arr)
  {
    this ();
    for (int i = 0;i < arr.length;i++)
      add (arr [i]);
  }

  public PDFArray (PDFArray array) {
    this ();
    for (int i = 0;i < array.size ();i++)
      add (array.get (i));
  }
  
  public void add (PDFObject o)
  {
    contents.add (o);
  }

  public void add (int i,PDFObject o)
  {
    contents.add (i,o);
  }

  public PDFObject set (int i,PDFObject o)
  {
    return contents.set (i,o);
  }

  public void remove (int index) {
    contents.remove (index);
  }
  
  public boolean isEmpty ()
  {
    return contents.isEmpty ();
  }

  public List<PDFObject> elements ()
  {
    return contents;
  }

  public PDFObject unresolvedElementAt (int index)
  {
    return contents.get (index);
  }

  // same as above if you know it's an indirect object
  public PDFIndirectObject indirectAt (int index)
  {
    return (PDFIndirectObject) contents.get (index);
  }

  public PDFObject get (int index)
  {
    return unresolvedElementAt (index).resolve ();
  }

  public int intAt (int index)
  {
    return ((PDFInteger) get (index)).val;
  }

  public double doubleAt (int index)
  {
    return ((PDFNumber) get (index)).doubleValue ();
  }

  public boolean booleanAt (int index)
  {
    return ((PDFBoolean) get (index)).val;
  }

  public float floatAt (int index)
  {
    return (float) doubleAt (index);
  }

  public String nameAt (int index)
  {
    return ((PDFName) get (index)).getName ();
  }

  public byte [] bytesAt (int index)
  {
    return ((PDFString) get (index)).getBytes ();
  }

  public double [] toDoubleArray ()
  {
    double [] arr = new double [contents.size ()];
    for (int i = 0;i < arr.length;i++)
      arr [i] = doubleAt (i);
    return arr;
  }

  public float [] toFloatArray ()
  {
    float [] arr = new float [contents.size ()];
    for (int i = 0;i < arr.length;i++)
      arr [i] = floatAt (i);
    return arr;
  }

  public int [] toIntArray ()
  {
    int [] arr = new int [contents.size ()];
    for (int i = 0;i < arr.length;i++)
      arr [i] = intAt (i);
    return arr;
  }

  public char [] toCharArray ()
  {
    char [] arr = new char [contents.size ()];
    for (int i = 0;i < arr.length;i++)
      arr [i] = (char) intAt (i);
    return arr;
  }

  public boolean [] toBooleanArray ()
  {
    boolean [] arr = new boolean [contents.size ()];
    for (int i = 0;i < arr.length;i++)
      arr [i] = booleanAt (i);
    return arr;
  }
  
  public PDFObject [] toArray () {
    return contents.toArray (new PDFObject [contents.size ()]);
  }

  public Rectangle toRectangle ()
  {
    return new Rectangle (toDoubleArray ());
  }

  static PDFArray toArray (PDFObject object)
  {
    if (object == null || object instanceof PDFArray)
      return (PDFArray) object;
    PDFArray result = new PDFArray ();
    result.add (object);
    return result;
  }
  
  public boolean contains (PDFObject object) {
	  for (PDFObject o : this)
		  if (o.equals (object))
			  return true;
	  return false;
  }

  public int size ()
  {
    return contents.size ();
  }

  PDFArray intersectionWith (PDFArray box)
  {
    Rectangle rect = toRectangle ();
    rect.intersectWith (box.toRectangle ());
    return new PDFArray (rect);
  }

  protected boolean write (PDFObjectWriter writer) throws IOException
  {
    writer.write ('[');
    writer.isDelimited = true;
    for (int i = 0;i < contents.size ();i++)
      writer.write (contents.get (i));
    writer.write (']');
    return true;
  }

  public String toString ()
  {
    StringBuilder stringBuilder = new StringBuilder ();

    stringBuilder.append ('[');
    for (int i = 0;i < contents.size ();i++)
      {
        if (i != 0)
          stringBuilder.append (' ');
        stringBuilder.append (contents.get (i));
      }
    stringBuilder.append (']');

    return stringBuilder.toString ();
  }

  protected String displayString ()
  {
    return "array";
  }

  void buildEditor (final Container container,final EditHandler editor)
  {
    container.setLayout (new GridLayout (size (),1));
    for (int i = 0;i < size ();i++)
      {
        final int index = i;
        container.add (editor.createLabel
                       (unresolvedElementAt (i)," [" + i + ']',
                        new Handler<PDFObject> () {
                            public void handle (PDFObject o)
                            {
                              if (o == null)
                                contents.remove (index);
                              else
                                set (index,o);
                            }
                          }));
      }
  }

  void diff (PDFContainer container,String name) throws IOException
  {
    PDFArray array = (PDFArray) container;
    if (array.size () == size ())
      for (int i = 0;i < size ();i++)
        diff (get (i),array.get (i),name + " [" + i + ']');
    else
      System.out.println ("size  diff : " + name);
  }

  boolean iterateInParallel (PDFContainer container,PairHandler handler) {
    for (int i = 0;i < size ();i++)
      if (!handler.handle (get (i),((PDFArray) container).get (i)))
        return false;
    return true;
  }
  
  public PDFArray clone () {
    return new PDFArray (this);
  }

  public boolean assimilate (PDFContainer container) {
    PDFArray array = (PDFArray) container;
    if (size () < container.size ()) {
      add (array.get (size ()));
      return true;
    }
    else if (size () > container.size ()) {
      remove (size () - 1);
      return true;
    }
    else
      for (int i = 0;i < size ();i++)
        if (!array.get (i).equals (get (i))) {
          set (i,array.get (i));
          return true;
        }
    return false;
  }
  
  void traverseFormFields (Handler<PDFDictionary> handler) {
    for (Object object : this) {
      PDFDictionary field = (PDFDictionary) object;
      handler.handle (field);
      PDFArray kids = (PDFArray) field.get ("Kids");
      if (kids != null)
        kids.traverseFormFields (handler);
    }
  }
}
