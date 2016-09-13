/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

/* This is 1D and 2D encoding in one class.
   We could separate them, but there's a
   (currently unimplemented) mode that mixes
   them, so it's probably best to keep everything
   together.
   Note that all three modes (1D, 2D and mixed)
   are implemented in Batik's TIFF decoder
   (org.apache.batik.ext.awt.image.codec.tiff.TIFFImage)
   and in Ghostscript's CCITTFaxDecode filter (scfd.c),
   and that they are often referred to as follows:
   pure 1-D : Group 3/G3/T4, 1-D
   mixed    : Group 3/G3/T4, 2-D
   pure 2-D : Group 4/G4/T6
   Both of those implementations report errors on the files
   in afail that cause KnownStreamCorruptedExceptions. */

package info.joriki.compression.ccitt;

import java.io.IOException;
import java.io.StreamCorruptedException;

import info.joriki.io.ByteSink;
import info.joriki.io.PeekableBitSource;
import info.joriki.io.KnownStreamCorruptedException;

import info.joriki.io.filter.Crank;

import info.joriki.util.Options;
import info.joriki.util.NotImplementedException;

import info.joriki.compression.huffman.HuffmanDecoder;
import info.joriki.compression.huffman.UndefinedHuffmanCodeException;

public class CCITTDecoder extends CCITTCoder implements Crank
{
  // like the codes, these are always the same and could be made
  // static if they are often used more than once.
  HuffmanDecoder modeDecoder;
  HuffmanDecoder [] runlengthDecoders = new HuffmanDecoder [2];

  int rows;
  boolean twoD;
  boolean invert;
  boolean byteAlign;

  PeekableBitSource source;
  ByteSink sink;

  int neol;
  int rowsDone;
  int currentColor;

  public CCITTDecoder (int columns,int rows,boolean twoD,boolean invert,boolean byteAlign)
  {
    super (columns);

    this.rows = rows;
    this.twoD = twoD;
    this.invert = invert;
    this.byteAlign = byteAlign;

    if (twoD)
      modeDecoder = new HuffmanDecoder (modeCode);
    for (int j = 0;j < 2;j++)
      runlengthDecoders [j] = new HuffmanDecoder (runlengthCodes [j]);

    if (twoD)
      reset ();
    else
      {
        state = MODE;
        currentColor = WHITE;
        a0 = 0;
      }
  }

  public void setSource (Object source)
  {
    this.source = (PeekableBitSource) source;
    
    if (twoD)
      modeDecoder.setSource (source);
    for (int j = 0;j < 2;j++)
      runlengthDecoders [j].setSource (source);
  }

  public void setSink (Object sink)
  {
    this.sink = (ByteSink) sink;
  }

  public void reset ()
  {
    state = MODE;
    aIndex = 0;
    newRow ();
    neol = 0;
  }

  final void toggleColor ()
  {
    currentColor = 1 - currentColor;
  }

  int runlength;

  int readRunlength () throws IOException
  {
    for (;;) {
      int term = runlengthDecoders [currentColor].read ();
      if (term < 0)
        return term;
      if (term == EOL_TERM) {
        if (twoD)
          throw new StreamCorruptedException ("misplaced EOL marker in 2D CCITT data");
        if (runlength != 0 || a0 != 0)
          throw new StreamCorruptedException ("misplaced EOL marker in 1D CCITT data");
        if (++neol == 6) { // RTC consists of 6 EOL markers
          state = CLOSED;
          return EOI;
        }
      }
      else {
        runlength += term;
        if (term < 64)
          return runlength;
      }
    }
  }
  
  boolean first;
  int bIndex;
  int aIndex;
  int a0;

  int state;
  final static int MODE = 0;
  final static int A0A1 = 1;
  final static int A0A1R = 2;
  final static int A1A2 = 3;
  final static int A1A2R = 4;
  final static int EOL_STATE = 5;
  final static int CLOSED = 6;
  final static int ZERO = 7;

  void endRow ()
  {
    if (++rowsDone == rows)
      // never happens if rows == 0
      state = CLOSED;
  }

  void newRow ()
  {
    for (int i = 0;i < 3;i++)
      scanlineChanges [aIndex++] = columns;
    int [] swap = referenceChanges;
    referenceChanges = scanlineChanges;
    scanlineChanges = swap;
    a0 = -1;
    first = true;
    aIndex = bIndex = 0;
    currentColor = WHITE;
  }

  void setChangeAt (int change)
  {
    scanlineChanges [aIndex++] = a0 = change;
  }

  int b (int i)
  {
    while (referenceChanges [bIndex] <= a0)
      bIndex++;
    if ((bIndex & 1) == currentColor)
      i--;
    return referenceChanges [bIndex + i];
  }

  public int crank () throws IOException
  {
    if (state == CLOSED)
      return EOI;

    if (!twoD) {
      int run = readRunlength ();
      if (run < 0)
        return run;
      runlength = 0;
      // less elegant but more efficient in preponderant case neol == 0
      if (neol > 0) {
        if (neol > 1)
          throw new StreamCorruptedException ("multiple EOL markers");
        neol = 0;
      }
      toggleColor ();
      run (run,currentColor);
      a0 += run;
      if (a0 > columns)
        throw new StreamCorruptedException ("too many pixels in row");
      if (a0 == columns) {
        endRow ();
        currentColor = WHITE;
        a0 = 0;
      }
    }
    else {
      try {
        switch (state) {
        case ZERO :
          new KnownStreamCorruptedException ("spurious zeros in CCITT data");
        case MODE :
          int mode = modeDecoder.read ();
          switch (mode) {
          case EOD :
          case EOI :
            return mode;
          case PASS :
            a0 = b (2);
            bIndex += 2;
            break;
          case HORIZONTAL :
            state = A0A1;
            return OK;
          case EXTENSION :
            throw new NotImplementedException ("CCITT Extension " + Integer.toBinaryString (source.readBits (3)));
          case EOL_MODE :
            // EOFB consists of 2 EOL markers, which we read separately to keep the code table size reasonable 
            state = EOL_STATE;
            return OK;
          default :
            int dx = mode - VERTICAL;
            if (!(-3 <= dx && dx <= 3))
              throw new StreamCorruptedException ("Illegal mode code " + mode + " in CCITT stream");
            setChangeAt (b (1) + dx);
            toggleColor ();
          }
          break;
        case A0A1 :
          runlength = 0;
          state = A0A1R;
          return OK;
        case A0A1R :
          int a0a1 = readRunlength ();
          if (a0a1 < 0)
            return a0a1;
          if (first)
            a0a1++;
          toggleColor ();
          setChangeAt (a0 + a0a1);
          state = A1A2;
          return OK;
        case A1A2 :
          runlength = 0;
          state = A1A2R;
          return OK;
        case A1A2R :
          int a1a2 = readRunlength ();
          if (a1a2 < 0)
            return a1a2;
          toggleColor ();
          setChangeAt (a0 + a1a2);
          state = MODE;
          break;
        case EOL_STATE :
          // read the second EOL making up EOFB
          int eol = modeDecoder.read ();
          if (eol < 0)
            return eol;
          if (eol != EOL_MODE)
            throw new StreamCorruptedException ("single EOL marker in 2D CCITT data");
          state = CLOSED;
          return EOI;
        default : throw new InternalError ();
        }
      } catch (UndefinedHuffmanCodeException uhce) {
        if (uhce.nextBits != 0)
          throw uhce;
        Options.warn ("unterminated CCITT data");
        // The second image on p. 46 in Unacooperativa.pdf
        // ends without Rows and without EOL, with the last
        // bit padded with zeros (the first image happens to
        // end on a byte boundary). Earlier there were also
        // some files that contained stretches of zeros where
        // more data is expected; this will cause an exception
        // above when crank is called again.
        state = ZERO;
        return EOI;
      }
    
      // this is reached after PASS, VERTICAL or A1A2,
      // i.e. after one atomic operation.
      first = false;
      if (a0 > columns)
        throw new KnownStreamCorruptedException ("Too many pixels in a row");
      if (a0 == columns) {
        int outColor = BLACK;
        int index = 0;
        int x = 0;
        while (x < columns) {
          int nextx = scanlineChanges [index++];
          run (nextx - x,outColor);
          outColor = 1 - outColor;
          x = nextx;
        }
        endRow ();
        newRow ();
        if (byteAlign)
          source.byteAlign ();
      }
    }

    return OK;
  }

  private void run (int run,int color) throws IOException
  {
    if (invert)
      color = 1 - color;
    while (run-- > 0)
      sink.write (color);
  }
}
