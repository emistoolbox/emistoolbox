/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.util.List;
import java.util.Iterator;

import info.joriki.util.General;
import info.joriki.util.Version;
import info.joriki.util.Constants;

public class ConstructiblePDFDocument extends PDFDocument
{
  public final static int NONE    = -1;
  public final static int ROMAN   = 0;
  public final static int roman   = 1;
  public final static int DECIMAL = 2;
  final static String [] rangeStyles = {"R","r","D"};

  final static PDFName fitH = new PDFName ("FitH");
  final static PDFInteger top = new PDFInteger (Constants.A4height + 4);
  final static PDFArray PDFprocSet = new PDFArray (new PDFObject [] {
    new PDFName ("PDF"),new PDFName ("Text")
      });
  public final static PDFArray A4MediaBox = new PDFArray (new int [] {
    0,0,Constants.A4width,Constants.A4height
  });

  public ConstructiblePDFDocument () {
    this (null,null,0);
  }
  
  public ConstructiblePDFDocument (int npages) {
    this (null,null,npages);
  }
  
  public ConstructiblePDFDocument (Version version,PDFArray mediaBox)
  {
    this (version,mediaBox,0);
  }
  
  public ConstructiblePDFDocument (Version version,PDFArray mediaBox,int npages) {
    super (new PDFDictionary ("Catalog"),version);
    PDFDictionary pages = new PDFDictionary ("Pages");
    pages.put ("Kids",new PDFArray ());
    pages.put ("Count",new PDFInteger ());
    pages.put ("Resources",new PDFDictionary ());
    pages.put ("MediaBox",mediaBox != null ? mediaBox : A4MediaBox);
    root.putIndirect ("Pages",pages);
    while (npages-- > 0) {
      PDFDictionary page = new PDFDictionary ("Page");
      page.putIndirect ("Contents",new PDFStream ());
      addPage (page);
    }
  }

  public void setMediaBox (PDFArray mediaBox) {
    ((PDFDictionary) root.get ("Pages")).put ("MediaBox",mediaBox);
  }

  public PDFDictionary addPage (PDFStream stream,List fonts)
  {
    PDFDictionary fontDict = new PDFDictionary ();
    for (int f = 0;f < fonts.size ();f++)
      fontDict.putIndirect ("F" + f,(PDFObject) fonts.get (f));
    addPage (stream,fontDict);
    return fontDict;
  }

  public void addPage (PDFStream content,PDFDictionary fontDictionary)
  {
    PDFDictionary page = new PDFDictionary ("Page");
    PDFDictionary resources = new PDFDictionary ();
    if (content != null)
      {
        resources.put ("Font",fontDictionary);
        resources.put ("ProcSet",PDFprocSet);
        page.putIndirect ("Contents",content);
      }
    page.put ("Resources",resources);
    addPage (page);
  }

  public void addPage (PDFDictionary page)
  {
    PDFDictionary pages = (PDFDictionary) root.get ("Pages");
    PDFArray kids = (PDFArray) pages.get ("Kids");
    page.putIndirect ("Parent",pages);
    kids.add (new PDFIndirectObject (page));
    pages.put ("Count",new PDFInteger (kids.size ()));
  }

  public void addPage (byte [] data) {
    PDFDictionary page = new PDFDictionary ("Page");
    page.putIndirect ("Contents",new PDFStream (data));
    addPage (page);
  }
  
  public void addEmptyPage ()
  {
    addPage (null,(PDFDictionary) null);
  }

  public void oddPage ()
  {
    PDFArray pageArray = getPageArray ();
    if ((pageArray.size () & 1) != 0)
      addEmptyPage ();
  }

  public PDFArray getPageRanges ()
  {
    PDFDictionary pageLabels = (PDFDictionary) root.get ("PageLabels");
    if (pageLabels == null)
      {
        pageLabels = new PDFDictionary ();
        pageLabels.put ("Nums",new PDFArray ());
        root.put ("PageLabels",pageLabels);
      }
    return (PDFArray) pageLabels.get ("Nums");
  }

  public void newPageLabel (String label)
  {
    newPageRange (NONE,0,label);
  }

  public void newPageRange (int style,int first)
  {
    newPageRange (style,first,null);
  }

  public void newPageRange (int style,int first,String prefix)
  {
    PDFDictionary range = new PDFDictionary ();
    if (prefix != null)
      range.put ("P",new PDFString (prefix));
    if (style != NONE)
      {
        range.put ("S",new PDFName (rangeStyles [style]));
        range.put ("St",new PDFInteger (first));
      }
    PDFArray pageRanges = getPageRanges ();
    pageRanges.add (new PDFInteger (getPageArray ().size ()));
    pageRanges.add (range);
  }

  public PDFIndirectObject getPageByLabel (String label)
  {
    char style;
    int num;
    try {
      num = Integer.parseInt (label);
      style = 'D';
    } catch (NumberFormatException nfe) {
      num = General.parseRoman (label);
      style = Character.isUpperCase (label.charAt (0)) ? 'R' : 'r';
    }

    Iterator ranges = getPageRanges ().iterator ();

    while (ranges.hasNext ())
      {
        int page = ((PDFInteger) ranges.next ()).val;
        PDFDictionary range = (PDFDictionary) ranges.next ();
        if (range.get ("S").toString ().charAt (0) == style)
          return (PDFIndirectObject) getPageArray ().get
            (page + num - range.getInt ("St",1));
      }
    return null;
  }

  public void ensureVersion (Version ensuredVersion)
  {
    if (ensuredVersion.moreRecentThan (version))
      version = ensuredVersion;
  }
}
