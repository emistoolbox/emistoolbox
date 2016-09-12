/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.cff;

import java.io.IOException;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;

import java.util.Map;
import java.util.TreeMap;
import java.util.Iterator;

import info.joriki.io.NybbleInputStream;
import info.joriki.io.NybbleOutputStream;

import info.joriki.util.General;
import info.joriki.util.Options;
import info.joriki.util.NotImplementedException;

// it just so happens that OtherBlues and FamilyOtherBlues are
// lexically greater than BlueValues and FamilyBlues, and so occur
// in the order demanded by the spec if we use a tree map :-)
public class CFFDict extends TreeMap implements CFFObject
{
  final static int ESCAPE = 0x100;
  final static int MASK   = 0x1ff;
  final static int SID    = 0x200;
  final Operator [] operators;
  final CFFFontSet fontSet;
  final Map defaultMap;

  public CFFDict (CFFFontSet fontSet,Operator [] operators)
  {
    this (fontSet,operators,null);
  }
  
  public CFFDict (CFFFontSet fontSet,Operator [] operators,Map defaultMap)
  {
    this.fontSet = fontSet;
    this.operators = operators;
    this.defaultMap = defaultMap;
  }

  Number [] stack = new Number [48];
  int nstack;

  final static int [] realByte = {
    '0','1','2','3','4','5','6','7','8','9','.','E','E',-1,'-'};

  void readFrom (InputStream in) throws IOException
  {
    readFrom (new DataInputStream (in));
  }

  void readFrom (DataInputStream dis) throws IOException
  {
    int next;
    while ((next = dis.read ()) != -1)
      if (next <= 27)
        {
          if (next == 0xc)
            next = dis.read () | ESCAPE;
          String name = nameFor (next);
          if (Options.tracing)
            System.out.println ("reading " + name);
          if ((codeFor (name) & SID) != 0)
          {
            if (!(nstack == 1 && stack [0] instanceof Integer))
              throw new Error ("SID should be an integer");
            put (name,fontSet.getString (((Integer) stack [0]).intValue ()));
          }
          else if (nstack == 1)
            put (name,stack [0]);
          else
          {
            Number [] ops = new Number [nstack];
            for (int i = 0;i < nstack;i++)
              ops [i] = stack [i];
            put (name,ops);
          }
          nstack = 0;
        }
      else if (next == 0x1c)
        stack [nstack++] = new Integer (dis.readShort ());
      else if (next == 0x1d)
        stack [nstack++] = new Integer (dis.readInt ());
      else if (next == 0x1e)
        {
          ByteArrayOutputStream real = new ByteArrayOutputStream ();
          NybbleInputStream nybbler = new NybbleInputStream (dis);
          int nybble;
    
          while ((nybble = nybbler.read ()) != 0xf)
            {
              real.write (realByte [nybble]);
              if (nybble == 0xc)
                real.write ('-');
            }
          stack [nstack++] = Double.valueOf (real.toString ());
        }
      else if (0x20 <= next && next <= 0xf6)
        stack [nstack++] = new Integer (next - 0x8b);
      else if (0xf7 <= next && next <= 0xfa)
        stack [nstack++] = new Integer (((next - 0xf7) << 8) + dis.read () + 0x6c);
      else if (0xfb <= next && next <= 0xfe)
        stack [nstack++] = new Integer (-(((next - 0xfb) << 8) + dis.read () + 0x6c));
      else
        throw new Error ("reserved CFF operator " + Integer.toHexString (next));
    if (Options.tracing)
      print ();
  }

  String nameFor (int code)
  {
    for (int i = 0;i < operators.length;i++)
      if ((operators [i].code & MASK) == code)
        return operators [i].name;
    throw new NotImplementedException ("operator code " + Integer.toHexString (code));
  }

  int codeFor (String name)
  {
    for (int i = 0;i < operators.length;i++)
      if (operators [i].name.equals (name))
        return operators [i].code;
    throw new Error ("invalid operator name " + name);
  }

  public void writeTo (ByteArrayOutputStream baos)
  {
    DataOutputStream dos = new DataOutputStream (baos);

    Iterator keys = keySet ().iterator ();
    Iterator values = values ().iterator ();

    while (keys.hasNext ())
      {
        try {
          write (dos,values.next ());
        } catch (IOException ioe) { throw new InternalError (); }
  
        String name = (String) keys.next ();
        int code = codeFor (name);
  
        if ((code & ESCAPE) != 0)
          baos.write (12);
        baos.write (code);
      }
  }
  
  void write (DataOutputStream dos,Object value) throws IOException // not really
  {
    if (value instanceof Number)
      {
        Number number = (Number) value;
        if (number.doubleValue () == number.intValue ())
          {
            int v = number.intValue ();
      
            if (-107 <= v && v <= 107)
              dos.write (v + 139);
            else if (108 <= v && v <= 1131)
              {
                v -= 108;
                dos.write ((v >> 8) + 247);
                dos.write (v);
              }
            else if (-108 >= v && v >= -1131)
              {
                v = -v;
                v -= 108;
                dos.write ((v >> 8) + 251);
                dos.write (v);
              }
            else if (-32768 <= v && v <= 32768)
              {
                dos.write (28);
                dos.writeShort (v);
              }
            else
              {
                dos.write (29);
                dos.writeInt (v);
              }
          }
        else
          {
            dos.write (30);
            byte [] arr = General.toString (number.doubleValue (),8).getBytes ();
            NybbleOutputStream nybbler = new NybbleOutputStream (dos);
            for (int i = 0;i < arr.length;i++)
              switch (arr [i])
                {
                case '.' :
                  nybbler.write (0xa);
                  break;
                case 'e' :
                case 'E' :
                  nybbler.write (0xb);
                  break;
                case '-' :
                  nybbler.write (0xe);
                  break;
                default :
                  nybbler.write (arr [i] - '0');
                }
            nybbler.write (0xf);
            nybbler.flush (0xf);
          }
      }
    else if (value instanceof Number [])
      {
        Number [] arr = (Number []) value;
        for (int i = 0;i < arr.length;i++)
          write (dos,arr [i]);
      }
    else if (value instanceof String)
      write (dos,new Integer (fontSet.getSID ((String) value)));
    else
      throw new NotImplementedException (value.getClass ().toString ());
  }

  void print ()
  {
    Iterator keys = keySet ().iterator ();
    Iterator values = values ().iterator ();
      
    while (keys.hasNext ())
      {
        System.out.print (keys.next () + " : ");
        Object value = values.next ();
        if (value instanceof Number)
          System.out.println (value);
        else if (value instanceof Number [])
          {
            Number [] arr = (Number []) value;
            System.out.print ("[");
            for (int i = 0;i < arr.length;i++)
              {
                if (i != 0)
                  System.out.print (' ');
                System.out.print (arr [i]);
              }
            System.out.println ("]");
          }
        else if (value instanceof String)
          System.out.println (value);
        else
          throw new Error ("unknown type of value : " + value);
      }
  }

  public Object get (Object key)
  {
    Object value = super.get (key);
    return value == null && defaultMap != null ? defaultMap.get (key) : value;
  }

  public void putNumber (String key,double value)
  {
    put (key,new Double (value));
  }

  public void putString (String key,String value)
  {
    put (key,value);
  }

  public void putBoolean (String key,boolean value)
  {
    put (key,new Integer (value ? 1 : 0));
  }

  public void putDelta (String key,double [] arr)
  {
    Number [] deltas = new Number [arr.length];
    for (int i = 0;i < arr.length;i++)
      deltas [i] = new Double (arr [i] - (i == 0 ? 0 : arr [i - 1]));
    put (key,deltas);
  }

  public void putArray (String key,double [] arr)
  {
    Number [] array = new Number [arr.length];
    for (int i = 0;i < arr.length;i++)
      array [i] = new Double (arr [i]);
    put (key,array);
  }

  public boolean equals (Object o)
  {
    return this == o;
  }

  // just anything that's final and still has Object.hashCode ()
  public int hashCode ()
  {
    return fontSet.hashCode ();
  }
}
