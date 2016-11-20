/*
 * Copyright 2003 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jp2;

import java.io.IOException;

import java.util.Iterator;

import info.joriki.util.Assertions;

public class BitReader
{
  int b;
  int c;
  int ct;

  Iterator segments;
  Segment segment;
  byte [] data;
  int pos;

  void initialize () throws IOException
  {
    b = c = 0;
    nextSegment ();
    nextByte ();
  }

  void nextSegment ()
  {
    segment = (Segment) segments.next ();
    data = segment.data;
    pos = 0;
  }

  /* This behavior is extremely badly described in the spec.
     It seems that data can be split up into segments without
     termination any way you want as long as each segment
     contains at least enough data to make all decisions
     required for the passes announced for it. Thus, you can
     pad with 0xff bytes to decode the earlier passes, but
     it messes up the decoder if you want to go on to decode
     the later ones. So for terminated segments we pad with
     0xff bytes until the decoder is re-initialized, whereas
     for unterminated segments we immediately skip to the next
     segment when the data in the current segment is used up. */

  void nextByte () throws IOException
  {
    ct = 8;
    int shift = 8;
    boolean special = b == 0xff;
    if (pos == data.length)
      {
	if (segment.terminal || !segments.hasNext ())
	  b = 0xff;
	else
	  {
	    nextSegment ();
	    nextByte ();
	    return;
	  }
      }
    else
      b = data [pos++] & 0xff;
    if (special)
      {
	if (b >= 0x90)
	  Assertions.expect (b,0xff);
	else
	  {
	    ct--;
	    shift++;
	  }
      }
    c += b << shift;
  }

  void nextBit () throws IOException
  {
    if (ct == 0)
      nextByte ();
    c <<= 1;
    ct--;
  }

  public boolean readBit (ArithmeticCodingContext context) throws IOException
  {
    nextBit ();
    return (c & 0x10000) != 0;
  }
}
