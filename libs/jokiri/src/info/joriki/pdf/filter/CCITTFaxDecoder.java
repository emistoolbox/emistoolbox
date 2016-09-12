/*
 * Copyright 2003 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf.filter;

import info.joriki.io.Readable;

import info.joriki.io.filter.BitBuffer;
import info.joriki.io.filter.ByteBuffer;
import info.joriki.io.filter.ByteFilters;
import info.joriki.io.filter.Concatenator;
import info.joriki.io.filter.BytesToBitsConverter;

import info.joriki.pdf.StreamFilter;
import info.joriki.pdf.PDFDictionary;

import info.joriki.util.NotImplementedException;

import info.joriki.compression.ccitt.CCITTDecoder;

public class CCITTFaxDecoder implements StreamFilter
{
  public Readable getReadable (Readable raw,PDFDictionary parameters)
  {
    int columns = parameters.getInt ("Columns",1728);
    int K = parameters.getInt ("K",0);
    if (K > 0)
      throw new NotImplementedException ("mixed CCITT encoding");

    boolean endOfLine = parameters.getBoolean ("EndOfLine",false);
    boolean endOfBlock = parameters.getBoolean ("EndOfBlock",true);
    boolean encodedByteAlign = parameters.getBoolean ("EncodedByteAlign",false);
    boolean blackIs1 = parameters.getBoolean ("BlackIs1",false);
    int rows = parameters.getInt ("Rows",0);
    int damagedRowsBeforeError = parameters.getInt ("DamagedRowsBeforeError",0);
    if (endOfLine != false ||
        endOfBlock != true ||
        damagedRowsBeforeError != 0)
      throw new NotImplementedException ("fancy CCITT parameters");
  
    // CCITTDecode produces bytes instead of bits for efficiency;
    // there is a special case for this in PDFImageDecoder.

    return ByteFilters.getReadable
    (Concatenator.concatenate
     (Concatenator.concatenate
      (Concatenator.concatenate
       (new BytesToBitsConverter (),
        new BitBuffer (false)),
       new CCITTDecoder (columns,rows,K < 0,blackIs1,encodedByteAlign)),
      new ByteBuffer ()),
     raw);

    /*
     The concatenation used to be as below, with the comment:
     "The order of concatenation is important to make sure
     the bit buffer doesn't need to buffer more than a byte."
    
      return ByteFilters.getReadable
      (Concatenator.concatenate
       (Concatenator.concatenate
        (new BytesToBitsConverter (),
         Concatenator.concatenate
         (new BitBuffer (false),
          new CCITTDecoder (columns,rows,K < 0,blackIs1,encodedByteAlign))),
        new ByteBuffer ()),
       raw);
    
     However, this led to the bit buffer overflowing when the CCITT decoder
     was done decoding but more data kept being requested by the byte buffer
     (e.g. due to a big array read in Util.copy). There is unfortunately no
     really clean way in the crank system for the bit buffer to signal "down"
     the chain to the byte buffer that it's reached the "end of output". One
     solution would have been for the bytes to bits converter to catch the
     I/O exception thrown by the bit buffer and to signal EOI (perhaps EOO?)
     to the byte buffer cranking it, another for the bytes to bits converter
     to catch EOI exceptions thrown by AtomicBuffer.outputCrank (). However,
     the new, more straightforward setup doesn't seem to lead to bit buffer
     overflows either, so this seems to be the best solution. I think the
     "opposite" concatenation below, which is just as straightforward as the
     one now being used, results in exactly the same crank setup; at least
     it produces the same stack trace in BytesToBitsConverter.crank ().
      
        return ByteFilters.getReadable
        (Concatenator.concatenate
         (new BytesToBitsConverter (),
          Concatenator.concatenate
          (new BitBuffer (false),
           Concatenator.concatenate
           (new CCITTDecoder (columns,rows,K < 0,blackIs1,encodedByteAlign),
            new ByteBuffer ()))),
         raw);
     */
  }
}
