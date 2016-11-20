/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.util.Iterator;

import info.joriki.util.General;
import info.joriki.util.Options;
import info.joriki.util.Assertions;
import info.joriki.util.CloneableObject;
import info.joriki.util.NotImplementedException;

import info.joriki.graphics.Transformation;

import info.joriki.adobe.LineStyles;

public class GraphicsState extends CloneableObject implements PaintTypes, LineStyles, DeviceColorSpaces
{
  public final static double defaultFlatness = 1;
  public final static double defaultSmoothness = .01; // off the top of my head
  public final static PDFObject defaultHalftone = null;
  public final static PDFDictionary defaultSoftMask = null;
  public final static String defaultBlendMode = "Normal";
  public final static PDFName defaultRenderingIntent = new PDFName ("RelativeColorimetric");
  public final static PDFColorSpace defaultColorSpace = deviceColorSpaces [GRAY];

  public TextState textState = new TextState ();
  public Transformation ctm = new Transformation ();
  public double [] dashPattern = new double [0];
  public double dashOffset = 0;
  public int lineJoin = 0;
  public int lineCap = 0;
  public double lineWidth = 1.;
  public double miterLimit = 10.;
  // The following five are length-2 arrays for stroking/non-stroking
  public PDFColorSpace [] colorSpace = {defaultColorSpace,defaultColorSpace};
  public float [] [] color = {{0},{0}};
  public PDFPattern [] pattern = new PDFPattern [2];
  public boolean [] overprint = new boolean [2]; // stroking/non since PDF 1.3
  public double [] alpha = {1,1};
  public int overprintMode = 0;
  public boolean strokeAdjustment = false;
  public boolean alphaIsShape = false;
  public PDFObject halftone = defaultHalftone;
  public PDFDictionary softMask = defaultSoftMask;
  public Transformation softMaskTransform;
  public PDFName renderingIntent = defaultRenderingIntent;
  public double flatness = defaultFlatness;
  public double smoothness = defaultSmoothness;
  public String blendMode = defaultBlendMode;

  public final static int blackGeneration = 0;
  public final static int transferFunction = 1;
  public final static int undercolorRemoval = 2;
  public final static String [] colorParameterKeys = {"BG","TR","UCR"};
  public final static String [] colorParameterNames = {
    "black generation","transfer function","undercolor removal"};
  public PDFObject [] colorParameters =
    new PDFObject [colorParameterNames.length];
  public PDFObject [] defaultColorParameters = 
    new PDFObject [] {null,new PDFName ("Identity"),null};

  public void set (PDFDictionary dictionary)
  {
    boolean opEncountered = false;

    Iterator keyIterator = dictionary.keys ().iterator ();

    outer :
      while (keyIterator.hasNext ())
        {
          String key = (String) keyIterator.next ();
          PDFObject value = dictionary.get (key);
  
          if (key.equals ("Type"))
            Assertions.expect (((PDFName) value).getName (),"ExtGState");
          else if (key.equals ("Name"))
            ; // ghostscript output has /Name entries.
          else if (key.equals ("AIS")) // alpha is shape
            alphaIsShape = ((PDFBoolean) value).val;
          else if (key.equals ("BM")) // blend mode
            {
              blendMode = ((PDFName) value).getName ();
              if (blendMode.equals ("Compatible"))
                blendMode = defaultBlendMode;
            }
          else if (key.equals ("SMask")) // soft mask
          {
            if (value instanceof PDFName)
            {
              Assertions.expect (((PDFName) value).getName (),"None");
              softMask = null;
            }
            else
            {  
              softMask = (PDFDictionary) value;
	      softMaskTransform = (Transformation) ctm.clone ();
              Assertions.expect (softMask.isOptionallyOfType ("Mask"));
            }
          }
          else if (key.equals ("TK")) // text knockout
            Assertions.expect (((PDFBoolean) value).val,true);
          else if (key.equals ("ca")) // non-stroking alpha
            alpha [NONSTROKING] = ((PDFNumber) value).doubleValue ();
          else if (key.equals ("CA")) // stroking alpha
            alpha [STROKING] = ((PDFNumber) value).doubleValue ();
          else if (key.equals ("HT") || // half tone
                   key.equals ("HT2")) // not in spec
          {
            if (value instanceof PDFName && ((PDFName) value).getName ().equals ("Default"))
              halftone = defaultHalftone;
            else
            {
              halftone = value;
              Options.warn ("halftone parameter ignored");
            }
          }
          else if (key.equals ("op")) // overprint
            {
              overprint [NONSTROKING] = ((PDFBoolean) value).val;
              opEncountered = true;
            }
          else if (key.equals ("OP")) // overprint
            {
              boolean val = ((PDFBoolean) value).val;
              overprint [STROKING] = val;
              if (!opEncountered)
                overprint [NONSTROKING] = val;
            }
          else if (key.equals ("OPM")) // overprint mode
            overprintMode = ((PDFInteger) value).val;
          else if (key.equals ("SA")) // stroke adjustment
            strokeAdjustment = ((PDFBoolean) value).val;
          else if (key.equals ("SM")) // smoothness tolerance
            smoothness = ((PDFNumber) value).doubleValue ();
          else if (key.equals ("FL")) // flatness
            flatness = ((PDFNumber) value).doubleValue ();
          else if (key.equals ("CALS_TR2")) // Business200409.pdf
          {
            Assertions.expect (((PDFName) value).getName (),"Default");
            Options.warn ("undefined key " + key + " in graphics state parameter dictionary");
          }
          else
            {
              for (int i = 0;i < colorParameters.length;i++)
                {
                  String oldKey = colorParameterKeys [i];
                  if (key.startsWith (oldKey))
                    {
                      String newKey = oldKey + "2";
                      boolean oldVersion = key.equals (oldKey);
                      boolean newVersion = key.equals (newKey);
                      Assertions.expect (oldVersion || newVersion);
                      if (oldVersion && dictionary.get (newKey) != null)
                        continue outer;
                      if (newVersion &&
                          (value instanceof PDFName) && 
                          ((PDFName) value).getName ().equals ("Default"))
                        value = defaultColorParameters [i];
                      else if (!value.equals (defaultColorParameters [i]))
                        Options.warn
                          (colorParameterNames [i] + " parameter ignored.");
                      colorParameters [i] = value;
                      continue outer;
                    }
                }
      
              throw new NotImplementedException ("graphics state parameter " + key);
            }
        }
  }

  public Object clone ()
  {
    GraphicsState clone = (GraphicsState) super.clone ();
    clone.textState = (TextState) textState.clone ();
    clone.ctm = (Transformation) ctm.clone ();
    clone.color = color.clone ();
    clone.pattern = pattern.clone ();
    clone.colorSpace = colorSpace.clone ();
    clone.overprint = overprint.clone ();
    clone.alpha = alpha.clone ();
    return clone;
  }

  public String toString ()
  {
    StringBuilder stringBuilder = new StringBuilder ();

    stringBuilder.append ("CTM         : ");
    stringBuilder.append (ctm);
    stringBuilder.append ('\n');
    stringBuilder.append ("line join   : ");
    stringBuilder.append (lineJoin);
    stringBuilder.append ('\n');
    stringBuilder.append ("line cap    : ");
    stringBuilder.append (lineCap);
    stringBuilder.append ('\n');
    stringBuilder.append ("line width  : ");
    stringBuilder.append (lineWidth);
    stringBuilder.append ('\n');
    stringBuilder.append ("miter limit : ");
    stringBuilder.append (miterLimit);
    stringBuilder.append ('\n');
    stringBuilder.append ("color       : ");
    stringBuilder.append ('\n');
    stringBuilder.append ("stroking    : ");
    General.append (stringBuilder,color [STROKING]);
    stringBuilder.append ('\n');
    stringBuilder.append ("nonstroking : ");
    General.append (stringBuilder,color [NONSTROKING]);
    stringBuilder.append ('\n');
    stringBuilder.append ("pattern     : ");
    stringBuilder.append ('\n');
    stringBuilder.append ("stroking    : ");
    stringBuilder.append (pattern [STROKING]);
    stringBuilder.append ('\n');
    stringBuilder.append ("nonstroking : ");
    stringBuilder.append (pattern [NONSTROKING]);
    stringBuilder.append ('\n');
    stringBuilder.append ("overprint   : ");
    stringBuilder.append ('\n');
    stringBuilder.append ("stroking    : ");
    stringBuilder.append (overprint [STROKING]);
    stringBuilder.append ('\n');
    stringBuilder.append ("nonstroking : ");
    stringBuilder.append (overprint [NONSTROKING]);
    stringBuilder.append ('\n');
    stringBuilder.append ("overprint mode : ");
    stringBuilder.append (overprintMode);
    stringBuilder.append ('\n');
    stringBuilder.append ("flatness    : ");
    stringBuilder.append (flatness);
    stringBuilder.append ('\n');
    stringBuilder.append ("smoothness    : ");
    stringBuilder.append (smoothness);
    stringBuilder.append ('\n');
    stringBuilder.append ("dash        : ");
    stringBuilder.append ('[');
    for (int i = 0;i < dashPattern.length;i++)
      {
        if (i != 0)
          stringBuilder.append (' ');
        stringBuilder.append (dashPattern [i]);
      }
    stringBuilder.append (']');
    stringBuilder.append (' ');
    stringBuilder.append (dashOffset);

    return stringBuilder.toString ();
  }

  public Transformation getTextRenderingMatrix ()
  {
    return new Transformation (textState.getTotalTextMatrix (),ctm);
  }

  public boolean blends ()
  {
    return !blendMode.equals (defaultBlendMode);
  }
}
