/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.io.IOException;
import java.io.InputStream;

import info.joriki.io.Resources;
import info.joriki.io.ByteToCharPacker;
import info.joriki.io.ByteArrayCharacterSource;

import info.joriki.cff.CFFFont;

import info.joriki.font.DescribedFont;
import info.joriki.graphics.Point;

import info.joriki.sfnt.SFNTFile;
import info.joriki.sfnt.SFNTTable;

import info.joriki.util.Options;
import info.joriki.util.Assertions;
import info.joriki.util.NotTestedException;

import info.joriki.adobe.CMapFile;
import info.joriki.adobe.ToUnicodeMap;
import info.joriki.adobe.CIDSystemInfo;

public class Type0Font extends PDFFont implements CFFFontContainer
{
  final static double [] defaultVerticalMetrics = {880,-1000};
  
  final static String cmaps = "cmaps";
  
  final static char CIDKEYED = '0';
  final static char TRUETYPE = '2';
  final static char OPENTYPE = '?';
  
  PDFDictionary descendant; // currently always a CID font dictionary
  PDFArray widthArray;
  CMapFile cmapFile;
  char subtype;
  
  double [] missingVerticalMetrics;
  PDFArray verticalMetrics;

  Type0Font (PDFDictionary fontDictionary)
  {
    super (fontDictionary,2);

    PDFArray descendants = (PDFArray) fontDictionary.get ("DescendantFonts");
    Assertions.expect (descendants.size (),1);
    descendant = (PDFDictionary) descendants.get (0);
    Assertions.expect (descendant.isOfType ("Font"));
    missingWidth = descendant.getInt ("DW",0);
    widthArray = (PDFArray) descendant.get ("W");
    if (descendant.contains ("DW2") || descendant.contains ("W2"))
      throw new NotTestedException ("non-standard vertical glyph metrics");
    missingVerticalMetrics = descendant.getDoubleArray ("DW2",defaultVerticalMetrics);
    verticalMetrics = (PDFArray) descendant.get ("W2");

    setFontDescriptor (descendant);
    Assertions.unexpect (fontDescriptor,null);
    
    String fullSubtype = descendant.getName ("Subtype");
    subtype = fullSubtype.charAt (fullSubtype.length () - 1);
    Assertions.expect (fullSubtype,"CIDFontType" + subtype);

    PDFDictionary CIDdictionary = (PDFDictionary) descendant.get ("CIDSystemInfo");
    CIDSystemInfo CIDsystemInfo = new CIDSystemInfo
        (CIDdictionary.getAsciiString ("Registry"),
         CIDdictionary.getAsciiString ("Ordering"),
         CIDdictionary.getInt ("Supplement"));

    PDFObject mapSpecification = descendant.get ("CIDToGIDMap");
    PDFObject encoding = fontDictionary.get ("Encoding");
    String descendantName = strip (descendant.getUTFName("BaseFont")); 

    // embedded CMap will cause a ClassCastException here
    String encodingName = ((PDFName) encoding).getName ();
    String strippedName = getStrippedName ();
    
    // lubesgreases200410-ae_32.pdf has CID fonts that contain an
    // undocumented /Name entry and don't follow the naming convention.
    if (descendant.getName ("Name") == null)
      // The first equality should hold for Type 2 CID fonts, the second for
      // Type 0 CID fonts (see Table 5.18), but p. 2 of 1594030243.pdf uses
      // a Type 0 CID font with the first equality.
      Assertions.expect (strippedName.equals (descendantName) ||
                         strippedName.equals (descendantName + '-' + encodingName));
    
    CIDdictionary .checkUnused ("5.13");
    descendant    .checkUnused ("5.14");
    fontDictionary.checkUnused ("5.18");
    
    vertical = encodingName.endsWith ("V");
    if (!vertical)
      Assertions.expect (encodingName.endsWith ("H"));

    if (!(encodingName.equals ("Identity-H") || encodingName.equals ("Identity-V"))) {
      InputStream in = Resources.getInputStream (Type0Font.class,"cmaps/" + encodingName);
      if (in == null)
        throw new Error ("unknown predefined CMap " + encodingName);
      try {
        cmapFile = new CMapFile (in);
      } catch (IOException e) {
        e.printStackTrace();
        throw new Error ("couldn't read CMap file");
      }
      if (fontDictionary.contains ("ToUnicode"))
        throw new NotTestedException ();
      else {
        in = Resources.getInputStream (Type0Font.class,
            "cmaps/" + CIDsystemInfo.registry + "-" + CIDsystemInfo.ordering + "-UCS2");
        if (in == null)
          throw new Error ("no ToUnicode map for " + encodingName);
        try {
          toUnicodeMap = new ToUnicodeMap (in,2);
        } catch (IOException e) {
          e.printStackTrace();
          throw new Error ("couldn't read toUnicode map");
        }
        // There seem to be new ones in C:\Programme\Adobe\Acrobat 7.0\Resource\CMap
      }
      if (!CIDsystemInfo.compatibleWith (cmapFile.getCIDSystemInfo ()))
        Options.warn ("incompatible CID system info");

      Assertions.expect (cmapFile.isVertical (),vertical);
    }

    char fontFileType;

    switch (subtype)
    {
    case CIDKEYED :
      fontFileType = '3';
      break;
    case TRUETYPE :
      fontFileType = '2';
      break;
    default :
      throw new Error ("Unknown subtype " + subtype);
    }

    stream = (PDFStream) fontDescriptor.get ("FontFile" + fontFileType);
    
    // 0060574860.pdf contains an OpenType font with embedded CFF
    // instead of a TrueType font. We want to catch this right away
    // and treat it as a CFF font henceforth, expect for purposes
    // of glyph indexing -- the OpenType spec says that CFF fonts
    // in OpenType files are indexed by GID.
    // (http://www.microsoft.com/typography/otspec/cff.htm)
    if (stream != null && subtype == TRUETYPE)
    {  
      SFNTFile sfntFile = (SFNTFile) getFontFile ();
      SFNTTable cffTable = sfntFile.getTable(SFNTFile.CFF);
      if (cffTable != null)
      {
        Assertions.expect (sfntFile.getTable (SFNTFile.GLYF),null);
        stream = new PDFStream (cffTable.toByteArray());
        fontFile = null;
        if (mapSpecification != null)
          throw new NotTestedException ("OpenType font with explicit CIDToGIDMap");
        if (toUnicodeMap == null)
          throw new NotTestedException ("OpenType font without ToUnicodeMap");
        subtype = OPENTYPE;
      }
    }
    
    if (mapSpecification instanceof PDFStream)
      {
        try {
          CIDtoGIDMap = new CIDToGIDMap ((PDFStream) mapSpecification);
        } catch (IOException ioe) {
          ioe.printStackTrace ();
          throw new Error ("can't construct CID to GID map");
        }
        Assertions.unexpect (stream,null);
      }
    else if (!(mapSpecification == null ||
               (mapSpecification instanceof PDFName &&
                ((PDFName) mapSpecification).getName ().equals ("Identity"))))
      throw new Error ("Illegal CIDToGIDMap specification " + mapSpecification);
  }

  CIDToGIDMap CIDtoGIDMap;

  protected DescribedFont readFontFile () throws IOException
  {
    return subtype == TRUETYPE ?
      (DescribedFont) readSFNTFile () :
      (DescribedFont) readCFFFile ();
  }

  public void fillWidthInterval ()
  {
    if (widthArray != null)
      {
        int i = 0;
        while (i < widthArray.size ())
          {
            int first = widthArray.intAt (i++);
            PDFObject object = widthArray.get (i++);
            if (object instanceof PDFArray)
              for (Object width : (PDFArray) object)
                widthInterval.add (((PDFNumber) width).doubleValue ());
            else
              widthInterval.add (((PDFNumber) widthArray.get (i++)).doubleValue ());
          }
      }
    widthInterval.add (missingWidth);
    widthInterval.min /= 1000;
    widthInterval.max /= 1000;
  }

  private double getWidthEntry (int cid)
  {
    if (widthArray != null) {
      int i = 0;
      while (i < widthArray.size ()) {
        int first = widthArray.intAt (i++);
        PDFObject object = widthArray.get (i++);
        if (object instanceof PDFArray) {
          PDFArray array = (PDFArray) object;
          if (first <= cid && cid < first + array.size ())
            return ((PDFNumber) array.get (cid - first)).doubleValue ();
        }
        else {
          int last = ((PDFInteger) object).val;
          PDFNumber width = (PDFNumber) widthArray.get (i++);
          if (first <= cid && cid <= last)
            return width.doubleValue ();
        }
      }
    }
    return missingWidth;
  }

  public double getGlyphWidth (int cid)
  {
    return getWidthEntry (cid) / 1000.;
  }

  private double getVerticalAdvanceEntry (int cid) {
    if (verticalMetrics != null) {
      int i = 0;
      while (i < verticalMetrics.size ()) {
        int first = widthArray.intAt (i++);
        PDFObject object = widthArray.get (i++);
        if (object instanceof PDFArray) {
          PDFArray array = (PDFArray) object;
          int ngroups = array.size () / 3;
          Assertions.expect (array.size (),3 * ngroups);
          if (first <= cid && cid < first + ngroups)
            return ((PDFNumber) array.get (3 * (cid - first))).doubleValue ();
        }
        else {
          int last = ((PDFInteger) object).val;
          PDFNumber width = (PDFNumber) widthArray.get (i++);
          PDFNumber x     = (PDFNumber) widthArray.get (i++);
          PDFNumber y     = (PDFNumber) widthArray.get (i++);
          if (first <= cid && cid <= last)
            return width.doubleValue ();
        }
      }
    }
    
    return missingVerticalMetrics [1];
  }
  
  public double getVerticalAdvance (int cid) {
    return getVerticalAdvanceEntry (cid) / 1000.;
  }
  
  private Point getPositionEntry (int cid) {
    if (verticalMetrics != null) {
      int i = 0;
      while (i < verticalMetrics.size ()) {
        int first = widthArray.intAt (i++);
        PDFObject object = widthArray.get (i++);
        if (object instanceof PDFArray) {
          PDFArray array = (PDFArray) object;
          int ngroups = array.size () / 3;
          Assertions.expect (array.size (),3 * ngroups);
          if (first <= cid && cid < first + ngroups)
            return new Point (
                ((PDFNumber) array.get (3 * (cid - first) + 1)).doubleValue (),
                ((PDFNumber) array.get (3 * (cid - first) + 2)).doubleValue ());
        }
        else {
          int last = ((PDFInteger) object).val;
          PDFNumber width = (PDFNumber) widthArray.get (i++);
          PDFNumber x     = (PDFNumber) widthArray.get (i++);
          PDFNumber y     = (PDFNumber) widthArray.get (i++);
          if (first <= cid && cid <= last)
            return new Point (x.doubleValue (),y.doubleValue ());
        }
      }
    }
    
    return new Point (getWidthEntry (cid) / 2,missingVerticalMetrics [0]);
  }
  
  public Point getPosition (int cid) {
    Point position = getPositionEntry (cid);
    position.x /= 1000;
    position.y /= 1000;
    return position;
  }

  public double getGlyphAdvance (int cid) {
    return vertical ? getVerticalAdvance (cid) : getGlyphWidth (cid);
  }

  public CharacterIterator getCharacterIterator (final byte [] text)
  {
    return new AbstractCharacterIterator () {
        ByteArrayCharacterSource cidSource = cmapFile == null ?
          new ByteToCharPacker (text) : cmapFile.getCharacterSource (text);
        int pos;

        public int nextCode ()
        {
          pos = cidSource.pos;
          return cidSource.read ();
        }

        public boolean onSpace ()
        {
          return cidSource.pos == pos + 1 && text [pos] == ' ';
        }
      };
  }

  public boolean containsCFF ()
  {
    return stream != null && subtype != TRUETYPE;
  }

  public byte [] getCFFData ()
  {
    return getRawData ();
  }

  public int getIndex (int cid)
  {
    return subtype == CIDKEYED ? ((CFFFont) getFontFile ()).GIDfor (new Integer (cid)) : 
           CIDtoGIDMap == null ? cid : CIDtoGIDMap.map (cid);
  }

  protected Object getGlyphSelector (int cid)
  {
    return new Integer (subtype == CIDKEYED || CIDtoGIDMap == null ? cid : CIDtoGIDMap.map (cid));
  }
}
