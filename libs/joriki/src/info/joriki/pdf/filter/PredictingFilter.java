/*
 * Copyright 2003 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf.filter;

import info.joriki.io.Readable;

import info.joriki.awt.image.png.PNGSpeaker;
import info.joriki.awt.image.png.PNGInputBuffer;

import info.joriki.pdf.StreamFilter;
import info.joriki.pdf.PDFDictionary;

import info.joriki.util.NotImplementedException;

abstract class PredictingFilter implements StreamFilter, PNGSpeaker
{
  public Readable getReadable (Readable raw,PDFDictionary parameters)
  {
    final Readable predictable = getPredictable (raw,parameters);

    if (parameters == null)
      return predictable;

    int colors           = parameters.getInt ("Colors",          1);
    int columns          = parameters.getInt ("Columns",         1);
    int bitsPerComponent = parameters.getInt ("BitsPerComponent",8);

    int predictor = parameters.getInt ("Predictor",1);

    if (predictor == 1)
      return predictable;
    else if (predictor == 2) {
      if (bitsPerComponent != 8)
        throw new NotImplementedException ("TIFF prediction with non-byte samples");
    }
    else if (!(10 <= predictor && predictor <= 15))
      throw new NotImplementedException ("Predictor " + predictor);
          
    boolean tiff = predictor == 2;
    return new PNGInputBuffer (colors,bitsPerComponent,columns,tiff ? SUB : VAR,tiff,predictable);
  }
    
  abstract Readable getPredictable (Readable raw,PDFDictionary parameters);
}
