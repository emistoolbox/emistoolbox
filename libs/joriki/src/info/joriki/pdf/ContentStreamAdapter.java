/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.io.IOException;
import java.util.List;

public class ContentStreamAdapter implements ContentStreamHandler
{
  public void setGraphicsState (PDFDictionary dictionary) {}

  public void setColorSpace (int paintType,PDFColorSpace colorSpace) {}
  public void setColor      (int paintType,float [] color) {}
  public void setPattern    (int paintType,PDFPattern pattern) {}

  public void setFlatness (double flatness) {}
  public void setSmoothness (double smoothness) {}

  public void setLineJoin (int join) {}
  public void setLineCap (int cap) {}
  public void setLineWidth (double width) {}
  public void setMiterLimit (double limit) {}
  public void setDash (double [] dash,double offset) {}
  public void setRenderingIntent (PDFName intent) {}

  public void concatenateMatrix (double [] matrix) {}

  public void gsave () {}
  public void grestore () {}

  public void moveTo (double x,double y) {}
  public void lineTo (double x,double y) {}
  public void curveTo (double [] coors) {}
  public void closePath () {}

  public void usePath (boolean stroke,int fillRule,int clipRule) {}

  public void beginTextObject () {}
  public void endTextObject () {}

  public void setTextMatrix (double [] matrix) {}
  public void setTextFont (PDFFont font,double size) {}
  public void setCharacterSpacing (double spacing) {}
  public void setWordSpacing (double spacing) {}
  public void setTextLeading (double leading) {}
  public void setHorizontalScaling (double scaling) {}
  public void setTextRenderingMode (TextRenderingMode renderingMode) {}
  public void setTextRise (double rise) {}

  public void moveToNextLine () {}
  public void moveToNextLine (double tx,double ty) {}

  public void show (byte [] text) {}
  public void show (List text) {}

  public void markContent (ContentMark contentMark) {}
  public void beginMarkedContent (ContentMark contentMark) {}
  public void endMarkedContent () {}

  public void shade (PDFShading shading) {}

  // this is a no-op that skips over any inline image data
  public void drawImage (PDFImage image)
    throws IOException
  {
    if (image.isInline ())
      new PDFImageDecoder (image).discard ();
  }

  public void drawForm (PDFStream formStream) {}

  // methods implemented by EquivalenceHandler
  public void drawRectangle (double x,double y,double width,double height) {}
  public void moveToNextLineAndShow (byte [] text) {}
  public void moveToNextLineAndSetTextLeading (double tx,double ty) {}
  public void closeAndUsePath (boolean stroke,int fillRule,int clipRule) {}

  public void setGlyphWidth (double x,double y) {}
  public void setGlyphMetrics (double [] metrics) {}

  public void beginPage () {}
  public void finishPage () {}

  public void beginContentStream () {}
  public void endContentStream () {}
}
