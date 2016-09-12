/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import info.joriki.util.Flags;
import info.joriki.util.NotImplementedException;
import info.joriki.util.Options;
import info.joriki.util.Assertions;

import info.joriki.graphics.Point;

public class PDFAnnotation
{
  public final static int INVISIBLE = 0;
  public final static int HIDDEN    = 1;
  public final static int PRINT     = 2;
  public final static int NOZOOM    = 3;
  public final static int NOROTATE  = 4;
  public final static int NOVIEW    = 5;
  public final static int READONLY  = 6;
  public final static int LOCKED    = 7;
  public final static int TOGGLE    = 8;

  public final static int MARKUP = 0;
  public final static int TEXT_MARKUP = 1;

  private final static Map typeMap = new HashMap ();

  static {
    Flags plain = new Flags ();
    Flags markup = new Flags (1 << MARKUP);
    Flags textMarkup = new Flags ((1 << MARKUP) | (1 << TEXT_MARKUP));

    typeMap.put ("Text"          ,markup);
    typeMap.put ("Link"          ,plain);
    typeMap.put ("FreeText"      ,markup);
    typeMap.put ("Line"          ,markup);
    typeMap.put ("Square"        ,markup);
    typeMap.put ("Circle"        ,markup);
    typeMap.put ("Polygon"       ,markup);
    typeMap.put ("PolyLine"      ,markup);
    typeMap.put ("Highlight"     ,textMarkup);
    typeMap.put ("Underline"     ,textMarkup);
    typeMap.put ("Squiggly"      ,textMarkup);
    typeMap.put ("StrikeOut"     ,textMarkup);
    typeMap.put ("Stamp"         ,markup);
    typeMap.put ("Caret"         ,markup);
    typeMap.put ("Ink"           ,markup);
    typeMap.put ("Popup"         ,plain);
    typeMap.put ("FileAttachment",markup);
    typeMap.put ("Sound"         ,markup);
    typeMap.put ("Movie"         ,plain);
    typeMap.put ("Widget"        ,plain);
    typeMap.put ("Screen"        ,plain);
    typeMap.put ("PrinterMark"   ,plain);
    typeMap.put ("TrapNet"       ,plain);
  }

  private final static float [] black = {0,0,0};

  public String type;
  public double [] box;
  public PDFAction action;
  public Set additionalActions;
  public Flags flags;
  public String name;
  public PDFDate modificationDate;
  public String title;         // markup only
  public PDFDate creationDate; // markup only
  public String subject;       // markup only
  public PDFDictionary annotationDictionary;
  PDFDocument dad;

  public PDFAnnotation (PDFDictionary annotationDictionary,PDFDocument dad)
  {
    this.annotationDictionary = annotationDictionary;
    this.dad = dad;
    if (annotationDictionary.isOfType ("Trans")) // Unacooperativa.pdf
      Options.warn ("invalid type for annotation dictionary");
    else
      Assertions.expect (annotationDictionary.isOptionallyOfType ("Annot"));
    type = annotationDictionary.getName ("Subtype");
    box = annotationDictionary.getRectangleArray ("Rect");
    flags = new Flags (annotationDictionary.getInt ("F",0));
    name = annotationDictionary.getTextString ("NM");
    modificationDate = annotationDictionary.getDate ("M");
    if (modificationDate == null)
    {
      modificationDate = annotationDictionary.getDate ("ModDate");
      if (modificationDate != null)
        Options.warn ("invalid key ModDate for annotation modification date");
    }
    if (isOfType (PDFAnnotation.MARKUP))
    {
      title = annotationDictionary.getTextString ("T");
      creationDate = annotationDictionary.getDate ("CreationDate");
      subject = annotationDictionary.getTextString ("Subj");
      annotationDictionary.ignore ("RC"); // rich content
    }
    if (type.equals ("Link"))
      // 2nd annotation on p. 1 of pcmag_test.pdf
      // T is not defined for Link annotations
      annotationDictionary.ignore ("T");

    PDFDictionary actionDictionary =
      (PDFDictionary) annotationDictionary.get ("A");
    if (actionDictionary != null)
      action = new PDFAction (actionDictionary,dad);
    additionalActions = PDFUtil.getAdditionalActions (annotationDictionary,dad);
    // There is a conflict between the AA entries in the widget annotation dictionary and
    // the field dictionary, which may be merged. If they are not merged, both may contain
    // an AA entry. If both additional action dictionaries exist and are non-empty, we need
    // to find out how to handle conflicts between them. For now, we make sure that at most
    // one is non-null and non-empty, and take the annotation's additional actions from this one.
    if (type.equals ("Widget"))
    {
      PDFDictionary fieldDictionary = (PDFDictionary) annotationDictionary.get ("Parent");
      Set fieldActions = fieldDictionary == null ? null : PDFUtil.getAdditionalActions (fieldDictionary,dad);
      if (isNullOrEmpty (additionalActions))
        additionalActions = fieldActions;
      else
        Assertions.expect (isNullOrEmpty (fieldActions));
    }
    // We can't call checkUnused here because type-specific entries haven't been used.
  }

  private boolean isNullOrEmpty (Set set)
  {
    return set == null || set.isEmpty ();
  }

  public PDFDestination getDestination ()
  {
    return PDFDestination.resolveDestination
      (annotationDictionary.get ("Dest"),dad);
  }

  public String getContents ()
  {
    PDFObject contents = annotationDictionary.get ("Contents");
    if (contents instanceof PDFName)
    {
      Options.warn ("Contents entry specifies name instead of string");
      return annotationDictionary.getName ("Contents");
    }
    return annotationDictionary.getTextString ("Contents",true);
  }

  public String getHexColor ()
  {
    return new DeviceRGBColorSpace ().toHexString (annotationDictionary.getFloatArray ("C",black));
  }

  public Point [] getQuadPoints ()
  {
    Assertions.expect (isOfType (TEXT_MARKUP));
    double [] coors = annotationDictionary.getDoubleArray ("QuadPoints");
    Assertions.expect (coors.length & 7,0);
    Point [] quadPoints = new Point [coors.length >> 1];
    for (int i = 0,j = 0;i < quadPoints.length;i++)
      quadPoints [i] = new Point (coors [j++],coors [j++]);
    return quadPoints;
  }

  public boolean isOfType (int typeFlag)
  {
    return isOfType (type,typeFlag);
  }

  static public boolean isOfType (String type,int typeFlag)
  {
    Flags typeFlags = (Flags) typeMap.get (type);
    if (typeFlags == null) 
      throw new NotImplementedException ("annotation type " + type);
    return typeFlags.getFlag (typeFlag);
  }
}

