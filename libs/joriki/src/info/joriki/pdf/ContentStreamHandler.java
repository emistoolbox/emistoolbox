/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.io.IOException;
import java.util.List;

import info.joriki.adobe.FillRules;
import info.joriki.adobe.LineStyles;

public interface ContentStreamHandler
  extends LineStyles, PaintTypes, FillRules, ContentStreamTypes, PDFOptions
{
  void setGraphicsState (PDFDictionary dictionary);

  void setColorSpace (int paintType,PDFColorSpace colorSpace);
  void setColor      (int paintType,float [] color);
  void setPattern    (int paintType,PDFPattern pattern);

  void setFlatness (double flatness);
  void setSmoothness (double smoothness);

  void setLineJoin (int join);
  void setLineCap (int cap);
  void setLineWidth (double width);
  void setMiterLimit (double limit);
  void setDash (double [] dash,double offset);
  void setRenderingIntent (PDFName intent);

  void concatenateMatrix (double [] matrix);

  void gsave ();
  void grestore ();

  void moveTo (double x,double y);
  void lineTo (double x,double y);
  void curveTo (double [] coors);
  void closePath ();

  void usePath (boolean stroke,int fillRule,int clipRule);

  void beginTextObject ();
  void endTextObject ();

  void setTextMatrix (double [] matrix);
  void setTextFont (PDFFont font,double size);
  void setCharacterSpacing (double spacing);
  void setWordSpacing (double spacing);
  void setTextLeading (double leading);
  void setHorizontalScaling (double scaling);
  void setTextRenderingMode (TextRenderingMode renderingMode);
  void setTextRise (double rise);

  void moveToNextLine ();
  void moveToNextLine (double tx,double ty);

  void show (byte [] text);
  void show (List text);

  void markContent (ContentMark contentMark);
  void beginMarkedContent (ContentMark contentMark);
  void endMarkedContent ();

  void shade (PDFShading shading);

  void drawImage (PDFImage image) throws IOException;

  void drawForm (PDFStream formStream);

  // methods implemented by EquivalenceHandler
  void drawRectangle (double x,double y,double width,double height);
  void moveToNextLineAndShow (byte [] text);
  void moveToNextLineAndSetTextLeading (double tx,double ty);
  void closeAndUsePath (boolean stroke,int fillRule,int clipRule);

  void setGlyphWidth (double x,double y);
  void setGlyphMetrics (double [] metrics);

  void beginPage ();
  void finishPage ();

  void beginContentStream ();
  void endContentStream ();
}
