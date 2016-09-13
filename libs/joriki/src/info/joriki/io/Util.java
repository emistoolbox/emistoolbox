/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.File;
import java.io.Reader;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilenameFilter;
import java.io.EOFException;
import java.io.PrintStream;

import java.util.Vector;

import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.ZipInputStream;

import info.joriki.util.General;
import info.joriki.util.Handler;
import info.joriki.util.Assertions;
import info.joriki.util.ThreadLocalBuffer;

public class Util
{
  private Util () {}

  /**
   * Returns an input stream which corresponds to the specified
   * readable object. This is useful when a class has read functionality
   * but cannot be an input stream because it is already something else.
   * <BR>
   * In the special case that <code>r</code> is itself an input
   * stream, this method returns <code>r</code> itself. Otherwise, it
   * constructs a corresponding readable input stream. 
   * @param r a readable object
   * @return a corresponding input stream
   */
  public static InputStream toInputStream (final Readable r)
  {
    return r instanceof InputStream ? (InputStream) r :
      new ReadableInputStream () {
        public int read () throws IOException
        {
          return r.read ();
        }

        public int read (byte [] b) throws IOException
        {
          return r.read (b);
        }

        public int read (byte [] b,int off,int len) throws IOException
        {
          return r.read (b,off,len);
        }

        public void close () throws IOException
        {
          r.close ();
        }

        public int available () throws IOException
        {
          return r.available ();
        }
      };
  }

  /**
   * Returns a readable object which corresponds to the specified
   * input stream.
   * <BR>
   * In the special case that <code>in</code> is itself a readable
   * object, this method returns <code>in</code> itself. Otherwise,
   * it constructs a corresponding readable input stream. 
   * @param in an inputs stream
   * @return a corresponding readable object
   */
  public static Readable toReadable (final InputStream in)
  {
    return in instanceof Readable ? (Readable) in :
      new ReadableInputStream () {
        public int read () throws IOException
        {
          return in.read ();
        }

        public int read (byte [] b) throws IOException
        {
          return in.read (b);
        }

        public int read (byte [] b,int off,int len) throws IOException
        {
          return in.read (b,off,len);
        }

        public void close () throws IOException
        {
          in.close ();
        }

        public int available () throws IOException
        {
          return in.available ();
        }
      };
  }

  /**
   * Returns an output stream which corresponds to the specified
   * writeable object. This is useful when a class has write functionality
   * but cannot be an output stream because it is already something else.
   * <BR>
   * In the special case that <code>w</code> is itself an output
   * stream, this method returns <code>w</code> itself. Otherwise, it
   * constructs a corresponding writeable output stream. 
   * @param w a writeable object
   * @return a corresponding output stream
   */
  public static OutputStream toOutputStream (final Writeable w)
  {
    if (w instanceof OutputStream)
      return (OutputStream) w;
    return new WriteableOutputStream () {
        public void write (int b) throws IOException
        {
          w.write (b);
        }

        public void write (byte [] b) throws IOException
        {
          w.write (b);
        }

        public void write (byte [] b,int off,int len) throws IOException
        {
          w.write (b,off,len);
        }

        public void close () throws IOException
        {
          w.close ();
        }
      };
  }

  static ThreadLocalBuffer defaultBuffer = new ThreadLocalBuffer (4096);
  
  /**
   * Copies data from the specified input stream to the specified
   * output stream until the end of file is reached.
   * @param in the input stream to be copied from
   * @param out the output stream to be copied to
   * @exception IOException if an I/O error occurs
   */
  public static void copy (InputStream in,OutputStream out) throws IOException
  {
    copy (in,out,defaultBuffer.getBuffer ());
  }
  
  /**
   * Copies data from the specified input stream to the specified
   * output stream until the end of file is reached.
   * @param in the input stream to be copied from
   * @param out the output stream to be copied to
   * @param size the buffer size to be used for copying
   * @exception IOException if an I/O error occurs
   */
  public static void copy (InputStream in,OutputStream out,int size) throws IOException
  {
    copy (in,out,new byte [size]);
  }

  /**
   * Copies data from the specified input stream to the specified
   * output stream until the end of file is reached.
   * @param in the input stream to be copied from
   * @param out the output stream to be copied to
   * @param buf the buffer to be used for copying
   * @exception IOException if an I/O error occurs
   */
  public static void copy (InputStream in,OutputStream out,byte [] buf) throws IOException
  {
    for (;;)
      {
        int read = in.read (buf);
        if (read <= 0)
          return;
        out.write (buf,0,read);
      }
  }

  /**
   * Copies one file to another.
   * @param source the source file
   * @param destination the destination file
   * @exception IOException if an I/O error occurs
   */
  public static void copy (File source,File destination) throws IOException
  {
    FileOutputStream out = new FileOutputStream (destination);
    try { copy (source,out); out.flush (); }
    finally { out.close (); }
  }

  /**
   * Copies the specified file to the specified output stream.
   * @param source the source file
   * @param out the output stream to be written to
   * @exception IOException if an I/O error occurs
   */
  public static void copy (File source,OutputStream out) throws IOException
  {
    FileInputStream in = new FileInputStream (source);
    try { copy (in,out); }
    finally { in.close (); }
  }

  /**
   * Copies the specified input stream to the specified file.
   * @param in the input stream to be read from
   * @param destination the name of the destination file
   * @exception IOException if an I/O error occurs
   */
  public static void copy (InputStream in,String destination) throws IOException
  {
    copy (in,new File (destination));
  }

  /**
   * Copies the specified input stream to the specified file.
   * @param in the input stream to be read from
   * @param destination the destination file
   * @exception IOException if an I/O error occurs
   */
  public static void copy (InputStream in,File destination) throws IOException
  {
    FileOutputStream out = new FileOutputStream (destination);
    try { copy (in,out); out.flush (); }
    finally { out.close (); }
  }

  /**
   * Copies data from the specified readable object to the specified
   * writeable object until the end of file is reached.
   * @param in the readable object to be copied from
   * @param out the writeable object to be copied to
   * @exception IOException if an I/O error occurs
   */
  public static void copy (Readable in,Writeable out) throws IOException
  {
    copy (in,out,defaultBuffer.getBuffer ());
  }

  /**
   * Copies data from the specified readable object to the specified
   * writeable object until the end of file is reached.
   * @param in the readable object to be copied from
   * @param out the writeable object to be copied to
   * @param size the buffer size to be used for copying
   * @exception IOException if an I/O error occurs
   */
  public static void copy (Readable in,Writeable out,int size)
    throws IOException
  {
    copy (in,out,new byte [size]);
  }

  /**
   * Copies data from the specified readable object to the specified
   * writeable object until the end of file is reached.
   * @param in the readable object to be copied from
   * @param out the writeable object to be copied to
   * @param buf the buffer to be used for copying
   * @exception IOException if an I/O error occurs
   */
  public static void copy (Readable in,Writeable out,byte [] buf)
    throws IOException
  {
    for (;;)
      {
        int read = in.read (buf);
        if (read <= 0)
          return;
        out.write (buf,0,read);
      }
  }

  /**
   * Creates the specified directory if it does not already exist.
   * @param dir the directory whose existence is to be ensured
   * @exception IOException if <code>dir</code> exists and is not
   * a directory, or if it does not exist and cannot be created
   */
  public static void ensureDirectoryExists (File dir) throws IOException
  {
    if (dir == null) // current directory
      ;
    else if (dir.exists ())
      {
        if (!dir.isDirectory ())
          throw new IOException (dir + " is not a directory");
      }
    else if (!dir.mkdir ())
      throw new IOException ("Cannot create directory " + dir);
  }

  /**
   * Creates the specified directory and all of its parent directories
   * if they do not already exist.
   * @param dir the directory the existence of whose path is to be ensured
   * @exception IOException if <code>dir</code> exists and is not
   * a directory, or if it or one of its parent directories does not exist
   * and cannot be created
   */
  public static void ensureDirectoryPathExists (File dir) throws IOException
  {
    File parent = dir.getParentFile ();
    if (parent != null && !parent.exists ())
      ensureDirectoryPathExists (parent);
    ensureDirectoryExists (dir);
  }

  /**
   * Skips exactly the specified number of bytes on the specified input stream.
   * This method insists on skipping exactly <code>n</code> bytes and loops
   * until it succeeds.
   * @param in the input stream on which bytes are to be skipped
   * @param n the number of bytes to be skipped
   * @exception IOException if an I/O error occurs
   */
  public static void skipBytesExactly
    (InputStream in,long n) throws IOException
  {
    while (n != 0)
      n -= in.skip (n);
  }
  
  public static void writeUTF
    (OutputStream out,byte [] arr) throws IOException
  {
    writeUTF (out,arr,0,arr.length);
  }

  public static void writeUTF
    (OutputStream out,byte [] arr,int off,int len) throws IOException
  {
    for (int i = 0;i < len;i++)
      {
        int b = arr [off + i] & 0xff;
        Assertions.unexpect (b,0);
        if (b < 0x80)
          out.write (b);
        else
          {
            out.write (0xC0 | ((b >> 6) & 0x1F));
            out.write (0x80 | ((b >> 0) & 0x3F));
          }
      }
  }

  // checks for absence of surrogates
  public static void checkUTF16 (byte [] arr) {
    Assertions.expect (arr.length & 1,0);
    for (int i = 0;i < arr.length;i += 2)
      Assertions.expect (arr [i] < (byte) 0xD8 || (byte) 0xDF < arr [i]);
  }
  
  public static void addRecursiveList (File dir,FilenameFilter filter,Vector v)
  {
    if (dir.isDirectory ())
      {
        String [] list = dir.list (filter);
        for (int i = 0;i < list.length;i++)
          addRecursiveList (new File (dir,list [i]),filter,v);
      }
    else
      v.add (dir);
  }

  public static File [] recursiveList (File dir,FilenameFilter filter)
  {
    Vector v = new Vector ();
    addRecursiveList (dir,filter,v);
    File [] files = new File [v.size ()];
    v.copyInto (files);
    return files;
  }

  public static int readBCD (ByteSource source,int nbytes) throws IOException
  {
    int result = 0;
    
    while (nbytes-- > 0)
      {
        int next = source.read ();
        if (next == -1)
          return -1;

        result *= 10;
        result += next >> 4;
        result *= 10;
        result += next & 15;
      }

    return result;
  }

  public static void skip (String str,Reader in) throws IOException
  {
    skip (str.toCharArray (),in);
  }

  // doesn't work for self-similar patterns
  public static void skip (char [] arr,Reader in) throws IOException
  {
    for (;;)
      {
        int i;
        for (i = 0;i < arr.length;i++)
          if (in.read () != (arr [i] & 0xffff))
            break;

        if (i == arr.length)
          return;
      }
  }

  public static int readMaximally (Readable in,byte [] buf,int off,int len)
    throws IOException
  {
    int read = 0;
    while (read != len)
    {
      int n = in.read (buf,off + read,len - read);
      if (n < 0)
        return read != 0 ? read : n;
      read += n;
    }
    return read;
  }

  public static byte [] readBytes (DataInput in,int nbytes) throws IOException
  {
    byte [] buf = new byte [nbytes];
    in.readFully (buf);
    return buf;
  }

  public static byte [] toByteArray (InputStream in) throws IOException
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    copy (in,baos);
    return baos.toByteArray ();
  }

  public static byte [] toByteArray (Outputable outputable)
  {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream ();
      outputable.writeTo (new DataOutputStream (baos));
      return baos.toByteArray ();
    } catch (IOException ioe) {
      ioe.printStackTrace ();
      throw new Error ("couldn't serialize outputable");
    }
  }

  public static byte [] toByteArray (Streamable streamable)
  {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream ();
      streamable.writeTo (baos);
      return baos.toByteArray ();
    } catch (IOException ioe) {
      ioe.printStackTrace ();
      throw new Error ("couldn't serialize streamable");
    }
  }

  public static String readString (DataInput in) throws IOException
  {
    return new String (readBytes (in,in.readUnsignedByte ()));
  }

  public static String readString (DataInput in,int length) throws IOException
  {
    return new String (readBytes (in,length));
  }

  public static void writeString (DataOutput out,String string) throws IOException
  {
    out.writeByte  (string.length ());
    out.writeBytes (string);
  }

  public static void hexDump (byte [] arr,String file) throws IOException
  {
    PrintStream ps = new PrintStream (new FileOutputStream (file));
    try { hexDump (arr,ps); ps.flush (); }
    finally { ps.close (); }
  }

  public static void hexDump (byte [] arr,PrintStream ps)
  {
    for (int i = 0;i < arr.length;i += 16)
      {
        for (int j = i;j < i + 16;j++)
          {
            try {
              byte next = arr [j];
              ps.print (General.toHexNybble (next >> 4));
              ps.print (General.toHexNybble (next));
            } catch (ArrayIndexOutOfBoundsException aioobe) {
              ps.print ("  ");
            }
            if ((j & 3) == 3)
              ps.print (" ");
          }
        ps.print ("   ");
        for (int j = i;j < i + 16;j++)
          {
            try {
              byte next = arr [j];
              if (General.isPrintable (next & 0xff))
                ps.write (next);
              else
                ps.write ('.');
            } catch (ArrayIndexOutOfBoundsException aioobe) {
              break;
            }
            if ((j & 3) == 3)
              ps.print (" ");
          }
        ps.println ();
      }
  }

  public static void hexDump (DataInput in,int nbytes) throws IOException
  {
    hexDump (readBytes (in,nbytes),System.out);
  }

  public static void hexDump (InputStream in,int nbytes) throws IOException
  {
    hexDump ((DataInput) new DataInputStream (in),nbytes);
  }

  public static void dump (byte [] arr,String filename) throws IOException
  {
    dump (arr,new File (filename));
  }

  public static void dump (byte [] arr,File file) throws IOException
  {
    FileOutputStream fos = new FileOutputStream (file);
    try { fos.write (arr); fos.flush (); }
    finally { fos.close (); }
  }

  public static byte [] undump (String filename) throws IOException
  {
    return undump (new File (filename));
  }

  public static byte [] undump (File file) throws IOException
  {
    FileInputStream in = new FileInputStream (file);
    try { return readBytes (new DataInputStream (in),(int) file.length ()); }
    finally { in.close (); }
  }

  public static byte [] undump (InputStream in) throws IOException
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    copy (in,baos);
    return baos.toByteArray ();
  }
  
  public static byte [] undump (Readable in) throws IOException
  {
    WriteableByteArray wba = new WriteableByteArray ();
    copy (in,wba);
    return wba.toByteArray ();
  }
  
  public static void dump (Outputable outputable,String filename) throws IOException
  {
    dump (outputable,new File (filename));
  }

  public static void dump (Outputable outputable,File file) throws IOException
  {
    FileOutputStream fos = new FileOutputStream (file);
    try { outputable.writeTo (new DataOutputStream (fos)); fos.flush (); }
    finally { fos.close (); }
  }

  public static void writeInt (int val,byte [] arr,int off,int len)
  {
    for (int j = len - 1;j >= 0;j--)
      {
        arr [off + j] = (byte) val;
        val >>= 8;
      }
  }

  public static void writeInteger (int val,byte [] arr,int off)
  {
    writeInt (val,arr,off,4);
  }

  public static void writeShort (int val,byte [] arr,int off)
  {
    writeInt (val,arr,off,2);
  }

  public static int readInt (byte [] arr,int off)
  {
    return readInteger (arr,off,4);
  }

  public static int readShort (byte [] arr,int off)
  {
    return readInteger (arr,off,2);
  }

  public static int readInteger (byte [] arr,int off,int len)
  {
    int lim = off + len;
    int val = 0;
    while (off < lim)
      {
        val <<= 8;
        val += arr [off++] & 0xff;
      }
    return val;
  }

  public static int readInteger (InputStream in,int len) throws IOException
  {
    int val = 0;
    while (len-- > 0)
      {
        val <<= 8;
        int r = in.read ();
        if (r == -1)
          throw new EOFException ();
        val |= r;
      }
    return val;
  }

  public static int readSignedInteger (InputStream in,int len) throws IOException
  {
    int r = in.read ();
    if (r == -1)
      throw new EOFException ();
    int val = (byte) r;
    while (--len > 0)
      {
        val <<= 8;
        r = in.read ();
        if (r == -1)
          throw new EOFException ();
        val |= r;
      }
    return val;
  }

  public static byte [] compress (byte [] rawData)
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    DeflaterOutputStream dos = new DeflaterOutputStream (baos);
    try {
      dos.write (rawData);
      dos.flush ();
      dos.close ();
    } catch (IOException ioe) { ioe.printStackTrace (); }
    return baos.toByteArray ();
  }

  public static byte [] compress (InputStream in)
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    DeflaterOutputStream dos = new DeflaterOutputStream (baos);
    try {
      copy (in,dos);
      dos.flush ();
      dos.close ();
    } catch (IOException ioe) { ioe.printStackTrace (); }
    return baos.toByteArray ();
  }

  public static void print (Object [] array)
  {
    for (int i = 0;i < array.length;i++)
      {
        if (array [i] instanceof Object [])
          print ((Object []) array [i]);
        else if (array [i] instanceof double [])
          System.out.println (General.toString ((double []) array [i]));
        else
          System.out.println (array [i]);
      }
  }

  public static InputStream getZipInputStream (ZipFile zipFile,String entry) throws IOException {
    ZipEntry zipEntry = zipFile.getEntry (entry);
    return zipEntry == null ? null : zipFile.getInputStream (zipEntry);
  }

  public static InputStream getZipInputStream (InputStream in,String entry) throws IOException {
    ZipInputStream zis = new ZipInputStream (in);
    for (;;) {
      ZipEntry zipEntry = zis.getNextEntry ();
      if (zipEntry == null)
        return null;
      if (zipEntry.getName ().equals (entry))
        return zis;
    }
  }

  public static File findFile (String dir,String name)
  {
    return findFile (new File (dir),name);
  }

  public static File findFile (File dir,String name)
  {
    File [] files = dir.listFiles ();
    if (files != null)
      for (int i = 0;i < files.length;i++)
        {
          if (files [i].isDirectory ())
            {
              File file = findFile (files [i],name);
              if (file != null)
                return file;
            }
          else if (files [i].getName ().equals (name))
            return files [i];
        }
    return null;
  }

  public static void createFromTemplate (String name,Class location,File directory,String [] [] substitutions) throws IOException
  {
    byte [] [] [] bytes = new byte [substitutions.length] [2] [];
    for (int i = 0;i < substitutions.length;i++)
      for (int j = 0;j < 2;j++)
        bytes [i] [j] = substitutions [i] [j].getBytes ();
    createFromTemplate (name,location,directory,bytes);
  }

  public static void createFromTemplate (String name,Class location,File directory,byte [] [] [] substitutions) throws IOException
  {
    byte [] content = Resources.getBytes (location,name);
    OutputStream out = new FileOutputStream (new File (directory,name));

    try {
      int pos = 0;
    outer:
      while (pos < content.length)
        {
          for (int i = 0;i < substitutions.length;i++)
            {
              byte [] substitution = substitutions [i] [0];
              int index;
              for (index = 0;index < substitution.length;index++)
                if (substitution [index] != content [pos + index])
                  break;
              if (index == substitution.length)
                {
                  // found match
                  out.write (substitutions [i] [1]);
                  pos += substitution.length;
                  continue outer;
                }
            }
          out.write (content [pos++]);
          out.flush ();
        }
    } finally { out.close (); }
  }

  public static void traverse (File file,Handler<File> handler)
  {
    if (file.isDirectory ())
      {
        File [] files = file.listFiles ();
        for (int i = 0;i < files.length;i++)
          traverse (files [i],handler);
      }
    else
      handler.handle (file);
  }

  public static String readLine ()
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    int c;
    try {
      while ((c = System.in.read ()) != '\n')
        if (c != '\r')
          baos.write (c);
      } catch (IOException ioe) {
        ioe.printStackTrace ();
        throw new Error ("couldn't read from standard input");
      }
    return baos.toString ();
  }
  
  public static String readZeroTerminatedString (InputStream in) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    int c;
    while ((c = in.read ()) != 0) {
      if (c == -1)
        throw new EOFException ();
      baos.write (c);
    }
    return baos.toString ();
  }
}
