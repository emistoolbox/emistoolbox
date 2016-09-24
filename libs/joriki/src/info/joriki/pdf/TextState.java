/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.util.List;

import info.joriki.util.Options;
import info.joriki.util.CloneableObject;

import info.joriki.graphics.Transformation;

public class TextState extends CloneableObject
{
  public PDFFont font;

  public double characterSpacing;
  public double wordSpacing;
  public double horizontalScaling = 1;
  public double leading;
  public double fontSize;
  public double textRise;

  public TextRenderingMode renderingMode = new TextRenderingMode (0);

  public Transformation textPrefix = new Transformation ();
  public Transformation textMatrix = new Transformation ();
  public Transformation textLineMatrix = new Transformation ();
  
  public String toString ()
  {
    StringBuilder stringBuilder = new StringBuilder ();

    stringBuilder.append ("text matrix   : ");
    stringBuilder.append (textMatrix);
    stringBuilder.append ("text line matrix   : ");
    stringBuilder.append (textLineMatrix);
    stringBuilder.append ('\n');
    stringBuilder.append ("character spacing  : ");
    stringBuilder.append (characterSpacing);
    stringBuilder.append ('\n');
    stringBuilder.append ("word spacing       : ");
    stringBuilder.append (wordSpacing);
    stringBuilder.append ('\n');
    stringBuilder.append ("horizontal scaling : ");
    stringBuilder.append (horizontalScaling);
    stringBuilder.append ('\n');
    stringBuilder.append ("leading            : ");
    stringBuilder.append (leading);
    stringBuilder.append ('\n');
    stringBuilder.append ("font               : ");
    stringBuilder.append (font);
    stringBuilder.append ('\n');
    stringBuilder.append ("font size          : ");
    stringBuilder.append (fontSize);
    stringBuilder.append ('\n');
    stringBuilder.append ("text rise          : ");
    stringBuilder.append (textRise);
    stringBuilder.append ('\n');
    stringBuilder.append ("rendering mode     : ");
    stringBuilder.append (renderingMode);

    return stringBuilder.toString ();
  }

  public void calculateTextPrefix ()
  {
    textPrefix =
      new Transformation (new double [] {fontSize * horizontalScaling,0,
                                         0,fontSize,
                                         0,textRise});
  }

  public Transformation getTotalTextMatrix ()
  {
    return new Transformation (textPrefix,textMatrix);
  }

  public void setTextFont (PDFFont font,double fontSize)
  {
    this.font = font;
    this.fontSize = fontSize;
    calculateTextPrefix ();
  }

  private void advanceBy (double advance)
  {
    int index;
    if (font.vertical)
      index = 2;
    else {
      index = 0;
      advance *= horizontalScaling;
    }
    textMatrix.matrix [4] += advance * textMatrix.matrix [index + 0];
    textMatrix.matrix [5] += advance * textMatrix.matrix [index + 1];
  }

  public double getAdvance (byte [] text)
  {
    double advance = 0;
    int nchar = 0;
    int nword = 0;

    CharacterIterator iterator = font.getCharacterIterator (text);
    int code;
    while ((code = iterator.next ()) >= 0)
      {
        advance += iterator.getAdvance ();
        nchar++;
        if (iterator.onSpace ())
          nword++;
      }
    return getAdvance (advance,nchar,nword);
  }

  public double getAdjustment (PDFNumber adjustment)
  {
    return -fontSize * adjustment.doubleValue () / 1000;
  }

  public void adjustBy (PDFNumber adjustment)
  {
    advanceBy (getAdjustment (adjustment));
  }

  public double getAdvance (CharacterIterator iterator)
  {
    return getAdvance (iterator.getAdvance (),1,iterator.onSpace () ? 1 : 0);
  }

  public void advanceBy (CharacterIterator iterator)
  {
    advanceBy (getAdvance (iterator));
  }

  public void advanceBy (byte [] text)
  {
    advanceBy (getAdvance (text));
  }

  public void advanceBy (List text)
  {
    double advance = 0;
    for (int i = 0;i < text.size ();i++)
      {
        Object o = text.get (i);
        if (o instanceof PDFNumber)
          advance += getAdjustment ((PDFNumber) o);
        else if (o instanceof byte [])
          advance += getAdvance ((byte []) o);
        else
          Options.warn ("Invalid argument " + o + " to TJ operator");
      }
    advanceBy (advance);
  }

  private double getAdvance (double advance,int nchar,int nword)
  {
    return 
      advance * fontSize +
      nchar * characterSpacing +
      nword * wordSpacing;
  }

  public double getScaledCharacterSpacing () {
    return characterSpacing / fontSize;
  }
}
