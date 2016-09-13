/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.DataOutput;
import java.io.IOException;

public class DataOutputMultiplexer implements DataOutput
{
  DataOutput out1;
  DataOutput out2;

  public DataOutputMultiplexer (DataOutput out1,DataOutput out2)
  {
    this.out1 = out1;
    this.out2 = out2;
  }

  public void writeInt (int i) throws IOException
  {
    out1.writeInt (i);
    out2.writeInt (i);
  }

  public void writeShort (int s) throws IOException
  {
    out1.writeShort (s);
    out2.writeShort (s);
  }

  public void writeBytes (String s) throws IOException
  {
    out1.writeBytes (s);
    out2.writeBytes (s);
  }

  public void write (int b) throws IOException
  {
    out1.write (b);
    out2.write (b);
  }

  public void write (byte [] b) throws IOException
  {
    out1.write (b);
    out2.write (b);
  }

  public void write (byte [] b,int off,int len) throws IOException
  {
    out1.write (b,off,len);
    out2.write (b,off,len);
  }

  public void writeBoolean (boolean b) throws IOException
  {
    out1.writeBoolean (b);
    out2.writeBoolean (b);
  }

  public void writeByte (int b) throws IOException
  {
    out1.writeByte (b);
    out2.writeByte (b);
  }

  public void writeChar (int c) throws IOException
  {
    out1.writeChar (c);
    out2.writeChar (c);
  }

  public void writeLong (long l) throws IOException
  {
    out1.writeLong (l);
    out2.writeLong (l);
  }

  public void writeFloat (float f) throws IOException
  {
    out1.writeFloat (f);
    out2.writeFloat (f);
  }

  public void writeDouble (double d) throws IOException
  {
    out1.writeDouble (d);
    out2.writeDouble (d);
  }

  public void writeChars (String s) throws IOException
  {
    out1.writeChars (s);
    out2.writeChars (s);
  }

  public void writeUTF (String s) throws IOException
  {
    out1.writeUTF (s);
    out2.writeUTF (s);
  }
}
