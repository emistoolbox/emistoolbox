/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.adobe;

import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.StreamCorruptedException;

import info.joriki.io.LoggingInputStream;

import info.joriki.util.General;
import info.joriki.util.Assertions;
import info.joriki.util.NotImplementedException;

public class AdobeStreamTokenizer extends AdobeSpeaker
{
  public byte [] bval;
  public double nval;
  public int ttype;

  public final static int TT_NAME    = -1;
  public final static int TT_STRING  = -2;
  public final static int TT_WORD    = -3;
  public final static int TT_INTEGER = -4;
  public final static int TT_REAL    = -5;
  public final static int TT_OPEN    = -6;
  public final static int TT_CLOSE   = -7;
  public final static int TT_EOF     = -8;

  InputStream in;
  boolean postScriptStyle;

  public AdobeStreamTokenizer (InputStream in)
  {
    this (in,false);
  }

  public AdobeStreamTokenizer (InputStream in,boolean postScriptStyle)
  {
    this.in = in;
    this.postScriptStyle = postScriptStyle;
  }

  int cache;
  boolean cached = false;

  void cache (int c)
  {
    cache = c;
    cached = true;
  }

  int cachedRead () throws IOException
  {
    if (cached)
      {
        cached = false;
        return cache;
      }
    return in.read ();
  }

  boolean backedUp;

  public void backUp ()
  {
    Assertions.unexpect (backedUp);
    backedUp = true;
  }

  public int nextToken () throws IOException
  {
    if (backedUp)
      backedUp = false;
    else
      ttype = readNextToken ();
    return ttype;
  }

  protected int readNextToken () throws IOException
  {
    int c;

    // read a (possibly cached) character, skipping white space and comments
    do
      {
        c = cachedRead ();
        if (c == '%')
          do
            c = in.read ();
          while (c != '\r' && c != '\n' && c != -1);
        if (c == -1)
          return TT_EOF;
      }
    while (ctype [c] == SPACE);

    ByteArrayOutputStream baos = null;

    switch (c)
      {
      case '/' :
        baos = new ByteArrayOutputStream ();
        for (;;)
          {
            c = in.read ();
            if (postScriptStyle && c == '/' && baos.size () == 0)
              throw new NotImplementedException ("immediately evaluated name");
            if (!isRegular (c))
              {
                cache (c);
                bval = baos.toByteArray ();
                return TT_NAME;
              }
            if (c == '#')
              {
                int first = in.read ();
                int second = in.read ();
                if (!(General.isHexDigit (first) &&
                      General.isHexDigit (second)))
                  throw new StreamCorruptedException
                    ("# not followed by two hexadecimal digits");
                c = General.hexByte (first,second);
              }
            baos.write (c);
          }
      case '(' : // literal string
        baos = new ByteArrayOutputStream ();
        int nest = 0;
        for (;;)
          {
            c = cachedRead ();
            switch (c)
              {
              case '\\' :
                c = in.read ();
                switch (c)
                  {
                  case 'n' : c = '\n'; break;
                  case 'r' : c = '\r'; break;
                  case 't' : c = '\t'; break;
                  case 'b' : c = '\b'; break;
                  case 'f' : c = '\f'; break;
                  case '\r' :
                    c = in.read ();
                    if (c != '\n')
                      cache (c);
                  case '\n' :
                    continue;
                  default :
                    if (!('0' <= c && c <= '7'))
                      break;
                    int result = 0;
                    for (int i = 0;'0' <= c && c <= '7' && i < 3;i++)
                      {
                        c -= '0';
                        result <<= 3;
                        result |= c;
                        c = in.read ();
                      }
                    cache (c);
                    c = result;
                  }
                break;
              case '\r' :
                c = in.read ();
                if (c != '\n')
                  {
                    cache (c);
                    c = '\n';
                  }
                break;
              case '(' :
                nest++;
                break;
              case ')' :
                nest--;
                break;
              }
            if (nest < 0)
              break;
            baos.write (c);
          }
        bval = baos.toByteArray ();
        return TT_STRING;
      case '<' :
        c = in.read ();
        if (c == '<') // open dictionary
          return TT_OPEN;
        // hexadecimal string
        baos = new ByteArrayOutputStream ();
        for (;;)
          {
            while (ctype [c] == SPACE)
              c = in.read ();
            if (!General.isHexDigit (c))
              break;
            int first = c;
            while (ctype [c = in.read ()] == SPACE)
              ;
            int second;
            if (General.isHexDigit (c))
              {
                second = c;
                c = in.read ();
              }
            else
              second = 0;

            baos.write (General.hexByte (first,second));
          }

        Assertions.expect (c,'>');
        bval = baos.toByteArray ();
        return TT_STRING;
      case '>' :
        c = in.read ();
        if (c == '>')
          return TT_CLOSE;
        cache (c);
        return '>';
      case '[' :
      case ']' :
      case '{' :
      case '}' :
        return c;
      default :
        Assertions.expect (isRegular (c));
        boolean beginsNumeric =
          c == '+' || c == '-' || c == '.' || ('0' <= c && c <= '9');
        if (postScriptStyle || !beginsNumeric)
          {
            baos = new ByteArrayOutputStream ();
            baos.write (c);
          }
        if (beginsNumeric)
          {
            // In PostScript, anything that's not a parseable number can
            // be used as a word. In PDF this isn't the case, since words
            // are limited to some well-known operators. So for PostScript
            // we need to accumulate the data for the number in case it turns
            // out to be a word after all, whereas for PDF this would be a waste.
            InputStream number = postScriptStyle ?
              new LoggingInputStream (in,baos) : in;
            boolean readDigit = false;
            boolean sign = c == '-';
            if (c == '+' || c == '-')
              c = number.read ();
            nval = 0;
            while ('0' <= c & c <= '9')
              {
                readDigit = true;
                nval *= 10;
                nval += c - '0';
                c = number.read ();
              }
            boolean real = c == '.';
            if (real)
              {
                double dec = 1;
                for (;;)
                  {
                    c = number.read ();
                    if (!('0' <= c && c <= '9'))
                      break;
                    readDigit = true;
                    dec *= .1;
                    nval += dec * (c - '0');
                  }
              }
            if (readDigit && !isRegular (c))
              {
                cache (c);
                if (sign)
                  nval = -nval;
                return real ? TT_REAL : TT_INTEGER;
              }
            // could be an exponent, or could be an initially numeric word
            // neither is allowed in PDF.
            if (!postScriptStyle)
              throw new StreamCorruptedException ("ill-formed number");
            if (c == 'e' || c == 'E')
              {
                c = number.read ();
                boolean negativeExponent = c == '-';
                if (c == '+' || c == '-')
                  c = number.read ();
                int exponent = 0;
                readDigit = false;
                while ('0' <= c & c <= '9')
                  {
                    readDigit = true;
                    exponent *= 10;
                    exponent += c - '0';
                    c = number.read ();
                  }
                if (readDigit && !isRegular (c))
                  {
                    cache (c);
                    while (exponent-- > 0)
                      nval *= negativeExponent ? .1 : 10;
                    return TT_REAL;
                  }
              }
          }

        // just a normal word
        while (isRegular (c = in.read ()))
          baos.write (c);
        cache (c);
        bval = baos.toByteArray ();
        return TT_WORD;
      }
  }

  private static boolean isRegular (int c) {
    return c != -1 && ctype [c] == REGULAR;
  }
  
  public int nextInt () throws IOException
  {
    Assertions.expect (nextToken (),TT_INTEGER);
    return (int) nval;
  }

  public int nextRawByte () throws IOException
  {
    return in.read ();
  }

  public void flush ()
  {
    cached = false;
  }

  public String toString ()
  {
    switch (ttype)
      {
      case TT_STRING : return "(" + new String (bval) + ")";
      case TT_NAME   : return "/" + new String (bval);
      case TT_WORD   : return new String (bval);
      default        : throw new NotImplementedException ("string for " + ttype);
      }
  }
}
