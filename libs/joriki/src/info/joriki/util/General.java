/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.util.Random;
import java.util.StringTokenizer;

public class General
{
  private General () {}
  
  public static int unitCount (int totalSize,int unitSize) {
    return (totalSize + unitSize - 1) / unitSize;
  }
  
  public static int bytesForBits (int bits)
  {
    return ((bits - 1) >> 3) + 1;
  }

  public static boolean isWhiteSpace (int c)
  {
    return c == ' ' || c == '\t' || c == '\r' || c == '\n' || c == '\f';
  }

  public static boolean isDigit (int c)
  {
    return '0' <= c && c <= '9';
  }

  public static boolean isUppercaseHexDigit (int c)
  {
    return
    ('0' <= c && c <= '9') ||
    ('A' <= c && c <= 'F');
  }

  public static boolean isHexDigit (int c)
  {
    return
      ('0' <= c && c <= '9') ||
      ('a' <= c && c <= 'f') ||
      ('A' <= c && c <= 'F');
  }

  public static int hexNybble (int c)
  {
    if ('0' <= c && c <= '9')
      return c - '0';
    if ('a' <= c && c <= 'f')
      return c - 'a' + 10;
    if ('A' <= c && c <= 'F')
      return c - 'A' + 10;
    throw new Error ("bad nybble : " + c);
  }

  public static char toHexNybble (int c)
  {
    c &= 0xf;
    return (char) (c < 10 ? c + '0' : c + 'a' - 10);
  }

  public static int hexByte (int c1,int c2)
  {
    return (hexNybble (c1) << 4) | hexNybble (c2);
  }

  public static boolean isLetter (int c)
  {
    return
      ('a' <= c && c <= 'z') ||
      ('A' <= c && c <= 'Z');
  }

  public static boolean isAlphanumeric (int c)
  {
    return isLetter (c) || isDigit (c);
  }

  public static boolean isPrintable (int c)
  {
    return 32 <= c && c <= 127;
  }

  public static int countBytes (int n)
  {
    int res = 1;
    while ((n >>= 8) != 0)
      res++;
    return res;
  }

  /*
    I 1
    V 5
    X 10
    L 50
    C 100
    D 500
    M 1000
  */

  private static int romanCode (char roman)
  {
    char upper = Character.toUpperCase (roman);
    for (int i = 0;i < romanNumerals.length;i++)
      if (upper == romanNumerals [i])
        return i;
    throw new NumberFormatException (roman + " is not a roman numeral");
  }

  static private int [] romanValue = {1,5,10,50,100,500,1000};
  static private char [] romanNumerals = {'I','V','X','L','C','D','M'};

  // It seems this is right and Adobe is wrong. The Web is
  // unanimously of the opinion that the decimal digits are
  // converted individually, and that 99 is XCIX, not IC.
  // See e.g. http://www.wilkiecollins.demon.co.uk/roman/1999.htm
  public static String toRomanNumerals (int n)
  {
    Assertions.expect (n >= 0);
    StringBuilder numerals = new StringBuilder ();
    while (n >= 1000)
      {
        numerals.append ('M');
        n -= 1000;
      }

    for (int index = 3,power = 1000;index >= 0;index--,power /= 10)
      {
        int digit = n / power;
        n -= digit * power;
        switch (digit)
          {
          case 5 :
          case 6 :
          case 7 :
          case 8 :
            numerals.append (romanNumerals [2*index + 1]);
            // fall through
          case 0 :
          case 1 :
          case 2 : 
          case 3 :
            for (int i = 0;i < digit % 5;i++)
              numerals.append (romanNumerals [2*index]);
            break;
          case 4 :
          case 9 :
            numerals.append (romanNumerals [2*index]);
            numerals.append (romanNumerals [2*index + (digit + 1) / 5]);
            break;
          default :
            throw new InternalError ();
          }
      }
    return numerals.toString ();
  }

  public static int parseRoman (String roman)
  {
    int last = 0;
    int max = -1;
    int res = 0;
    boolean usedNegative = false;
    int same = 0; // initial value not used

    for (int i = roman.length () - 1;i >= 0;i--)
      {
        char letter = roman.charAt (i);
        int code = romanCode (letter);
        int value = romanValue [code];
        if (code > max)
          {
            res += value;
            last = max;
            max = code;
            usedNegative = false;
            same = 1;
          }
        else
          {
            if (code < max && usedNegative)
              throw new NumberFormatException ("only one prefix subtraction allowed in roman numerals");
      
            if (code == max)
              {
                int maxrep = ((code & 1) == 0) ? 3 : 1;
                if (++same > maxrep)
                  throw new NumberFormatException ("at most " + maxrep + " " + letter + " in roman numerals");
                res += value;
              }
            else
              {
                if (code == last)
                  throw new NumberFormatException ("can't have positive and negative versions of " + letter);
                if (code != 0 && code != 2 && code != 4)
                  throw new NumberFormatException ("only I, X, C can be used subtractively");
                res -= value;
                usedNegative = true;
              }
          }
      }
      
    return res;
  }

  public static List union (List v1,List v2)
  {
    List res = new ArrayList ();
    res.addAll (v1);
    res.addAll (v2);
    return res;
  }

  public static String conciseString (double v)
  {
    int iv = (int) v;
    return iv == v ? Integer.toString (iv) : Double.toString (v);
  }

  private static int shift (double v)
  {
    int shift = 0;

    double power = 1;
    double abs = v < 0 ? -v : v;
    while (abs >= power)
      {
        power *= 10;
        shift++;
      }
    while (abs < (power *= .1))
      shift--;

    return shift;
  }

  public static String preciseString (double v)
  {
    return preciseString (v,15);
  }

  public static String preciseString (double v,int digits)
  {
    return v == 0 ? "0" : toString (v,digits - shift (v));
  }

  public static void append (StringBuilder stringBuilder,float d)
  {
    if (d == 0)
      stringBuilder.append ('0');
    else
      append (stringBuilder,d,7 - shift (d));
  }

  public static void append (StringBuilder stringBuilder,double d)
  {
    if (d == 0)
      stringBuilder.append ('0');
    else
      append (stringBuilder,d,15 - shift (d));
  }

  public static void append (StringBuilder stringBuilder,double d,int digits)
  {
    boolean negative = d < 0;
    if (negative)
      d = -d;

    if (d == Double.POSITIVE_INFINITY)
      throw new IllegalArgumentException ("trying to print infinity");

    double lowest = 1;
    while (digits-- > 0)
      lowest *= .1;

    d += .5 * lowest;

    if (d < lowest)
      {
        stringBuilder.append (0);
        return;
      }

    if (negative)
      stringBuilder.append ('-');

    double power = 1;
    while (power <= d)
      power *= 10;
    
    while (d >= lowest || power > 1)
      {
        if (power == 1)
          stringBuilder.append ('.');
        power *= .1;
        int digit = (int) (d / power);
        stringBuilder.append ((char) ('0' + digit));
        d -= digit * power;
      }
  }

  public static void append (StringBuilder stringBuilder,float [] arr)
  {
    for (int i = 0;i < arr.length;i++)
      {
        if (i != 0)
          stringBuilder.append (' ');
        append (stringBuilder,arr [i]);
      }
  }

  public static void append (StringBuilder stringBuilder,double [] arr)
  {
    for (int i = 0;i < arr.length;i++)
      {
        if (i != 0)
          stringBuilder.append (' ');
        append (stringBuilder,arr [i]);
      }
  }

  public static void append (StringBuilder stringBuilder,double [] arr,int digits)
  {
    for (int i = 0;i < arr.length;i++)
      {
        if (i != 0)
          stringBuilder.append (' ');
        append (stringBuilder,arr [i],digits);
      }
  }

  public static void append (StringBuilder stringBuilder,int [] arr)
  {
    for (int i = 0;i < arr.length;i++)
      {
        if (i != 0)
          stringBuilder.append (' ');
        stringBuilder.append (arr [i]);
      }
  }

  public static void append (StringBuilder stringBuilder,byte [] arr) {
    for (int i = 0;i < arr.length;i++)
      stringBuilder.append (isPrintable(arr [i]) ? (char) arr [i] : '.');
  }
  
  public static String toString (double val,int digits)
  {
    StringBuilder stringBuilder = new StringBuilder ();
    append (stringBuilder,val,digits);
    return stringBuilder.toString ();
  }

  public static String toString (float [] arr)
  {
    StringBuilder stringBuilder = new StringBuilder ();
    append (stringBuilder,arr);
    return stringBuilder.toString ();
  }

  public static String toString (double [] arr)
  {
    StringBuilder stringBuilder = new StringBuilder ();
    append (stringBuilder,arr);
    return stringBuilder.toString ();
  }

  public static String toString (double [] arr,int digits)
  {
    StringBuilder stringBuilder = new StringBuilder ();
    append (stringBuilder,arr,digits);
    return stringBuilder.toString ();
  }

  public static String toString (Object [] objects) {
    StringBuilder stringBuilder = new StringBuilder ();
    for (Object object : objects) {
      if (stringBuilder.length () != 0)
        stringBuilder.append (' ');
      stringBuilder.append (object);
    }
    return stringBuilder.toString ();
  }
  
  public static String toString (Appendable a)
  {
    StringBuilder stringBuilder = new StringBuilder ();
    a.appendTo (stringBuilder);
    return stringBuilder.toString ();
 }

  public static String toString (Object o) {
    if (o instanceof Float)
      return preciseString ((Float) o);
    if (o instanceof Double)
      return preciseString ((Double) o);
    if (o instanceof float [])
      return toString ((float []) o);
    if (o instanceof double [])
      return toString ((double []) o);
    if (o instanceof Object [])
      return toString ((Object []) o);
    if (o instanceof Appendable)
      return toString ((Appendable) o);
    return o.toString ();
  }

  public static byte [] toBytes (String string,int length) {
    byte [] bytes = string.getBytes ();
    byte [] longBytes = new byte [length];
    System.arraycopy (bytes,0,longBytes,0,Math.min (bytes.length,longBytes.length));
    return longBytes;
  }
  
  public static <T> void traverse (Collection<T> collection,Handler<T> handler)
  {
    Iterator<T> iterator = collection.iterator ();
    while (iterator.hasNext ())
      handler.handle (iterator.next ());
  }

  public static boolean isPowerOfTwo (int n)
  {
    return (n & (n - 1)) == 0 && n != 0 && n != 0x80000000;
  }

  public static String pad (String str,char padding,int length,int align)
  {
    boolean left = align == Alignment.LEFT;
    boolean toggle = align == Alignment.CENTER;
    while (str.length () < length)
      {
        str = left ? str + padding : padding + str;
        left ^= toggle;
      }
    return str;
  }

  public static String pad (int n,char padding,int length,int align)
  {
    return pad (Integer.toString (n),padding,length,align);
  }

  public static String zeroPad (String str,int digits)
  {
    return pad (str,'0',digits,Alignment.RIGHT);
  }

  public static String spacePad (String str,int digits)
  {
    return pad (str,' ',digits,Alignment.RIGHT);
  }

  public static String zeroPad (int n,int digits)
  {
    return zeroPad (Integer.toString (n),digits);
  }

  public static String replace (String str,char from,String to)
  {
    return replace (str,new String (new char [] {from}),to);
  }

  public static String replace (String str,String from,String to)
  {
    StringBuilder stringBuilder = new StringBuilder ();

    int length = from.length ();
    int lastEnd = 0;
    int index;

    while ((index = str.indexOf (from,lastEnd)) != -1)
      {
        stringBuilder.append (str.substring (lastEnd,index)).append (to);
        lastEnd = index + length;
      }
    
    return stringBuilder.append (str.substring (lastEnd)).toString ();
  }

  public static String readNullTerminatedString (InputStream in) throws IOException
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    int c;
    while ((c = in.read ()) != 0)
      baos.write (c);
    return baos.toString ();
  }

  public static int compare (byte [] arr1,byte [] arr2)
  {
    for (int i = 0;i < arr1.length && i < arr2.length;i++)
      {
        int diff = (arr1 [i] & 0xff) - (arr2 [i] & 0xff);
        if (diff != 0)
          return diff;
      }

    return arr1.length - arr2.length;
  }

  public static boolean contains (byte [] arr,byte [] key)
  {
    outer :
      for (int i = 0;i <= arr.length - key.length;i++)
        {
          for (int j = 0;j < key.length;j++)
            if (arr [i + j] != key [j])
              continue outer;
          return true;
        }
    return false;
  }

  public static void sleep (int n)
  {
    try {
      Thread.sleep (n);
    } catch (InterruptedException ie) {}
  }

  public static Integer [] toIntegerArray (int [] arr)
  {
    Integer [] integers = new Integer [arr.length];
    for (int i = 0;i < arr.length;i++)
      integers [i] = new Integer (arr [i]);
    return integers;
  }

  public static int [] toIntArray (List list)
  {
    int [] result = new int [list.size ()];
    for (int i = 0;i < result.length;i++)
      result [i] = ((Integer) list.get (i)).intValue ();
    return result;
  }

  public static double [] toDoubleArray (List list)
  {
    double [] result = new double [list.size ()];
    for (int i = 0;i < result.length;i++)
      result [i] = ((Number) list.get (i)).doubleValue ();
    return result;
  }

  public static char [] toCharArray (List list)
  {
    char [] result = new char [list.size ()];
    for (int i = 0;i < result.length;i++)
      result [i] = ((Character) list.get (i)).charValue ();
    return result;
  }

  public static void setSize (List list,int size)
  {
    while (list.size () > size)
      list.remove (list.size () - 1);
    while (list.size () < size)
      list.add (null);
  }

  public static Object set (List list,int index,Object object)
  {
    if (index >= list.size ())
      setSize (list,index + 1);
    return list.set (index,object);
  }

  public static char [] packIntoChars (byte [] arr)
  {
    Assertions.expect (arr.length & 1,0);
    char [] carr = new char [arr.length >> 1];
    for (int i = 0,j = 0;i < carr.length;i++,j += 2)
      carr [i] = (char) ((arr [j] << 8) | (arr [j+1] & 0xff));
    return carr;
  }

  public static int toInteger (byte [] arr)
  {
    return toInteger (arr,0,arr.length);
  }

  public static int toInteger (byte [] arr,int off,int len)
  {
    Assertions.limit (len,0,4);

    int result = 0;
    for (int i = 0;i < len;i++)
      {
        result <<= 8;
        result |= arr [off + i] & 0xff;
      }
    return result;
  }

  public static byte [] toByteArray (int c,int n)
  {
    byte [] bytes = new byte [n];
    for (int i = 1;i <= n;i++)
      {
        bytes [n - i] = (byte) c;
        c >>= 8;
      }
    return bytes;
  }

  public static int numberIndex (String str,int off)
  {
    return numberIndex (str,off,true,false);
  }

  public static int numberIndex (String str,int off, boolean allowReal,boolean allowHex)
  {
    try {
      for (;;)
        {
          char c = str.charAt (off);
          if (!(c == '+' || c == '-' ||
                (allowReal && c == '.') ||
                isDigit (c) ||
                (allowHex && isHexDigit (c))))
            return off;
          off++;
        }
    } catch (IndexOutOfBoundsException ioobe) {
      return str.length ();
    }
  }

  public static Object [] deepArrayClone (Object [] array)
  {
    Object [] clone = array.clone ();

    for (int i = 0;i < clone.length;i++)
      {
        Object object = clone [i];
        if (object instanceof Object [])
          clone [i] = deepArrayClone ((Object []) clone [i]);
        else if (object instanceof int [])
          clone [i] = ((int []) object).clone ();
        else if (object instanceof double [])
          clone [i] = ((double []) object).clone ();
        else
          throw new NotImplementedException ("cloning " + object.getClass ());
      }
    return clone;
  }

  public static List nullList (int n)
  {
    ArrayList list = new ArrayList ();
    while (n-- > 0)
      list.add (null);
    return list;
  }

  public static double [] getDoubleArray (Map dictionary,Object key)
  {
    List list = (List) dictionary.get (key);
    if (list == null)
      return null;
    double [] array = new double [list.size ()];
    for (int i = 0;i < array.length;i++)
      array [i] = ((Number) list.get (i)).doubleValue ();
    return array;
  }

  public static String unqualifiedName (Class c)
  {
    String name = c.getName ();
    int index = name.lastIndexOf ('.');
    return index == -1 ? name : name.substring (index + 1);
  }

  public static <T> T removeLast (List<T> list)
  {
    return list.remove (list.size () - 1);
  }

  public static List subsets (Set s)
  {
    int n = s.size ();
    Assertions.limit (n,0,31);
    List subsets = new ArrayList ();
    long nsub = 1L << n;
    for (long i = 0;i < nsub;i++)
      {
        Iterator iterator = s.iterator ();
        Set subset = new HashSet ();
        for (int k = 0,bit = 1;k < n;k++,bit <<= 1)
          {
            Object element = iterator.next ();
            if ((i & bit) != 0)
              subset.add (element);
          }
        subsets.add (subset);
      }
    return subsets;
  }

  public static int bitLength (int v)
  {
    Assertions.expect (v >= 0);

    int bits = 0;
    
    while (v != 0)
      {
        v >>= 1;
        bits++;
      }

    return bits;
  }

  public static int indexLength (int limit)
  {
    return limit == 1 ? 1 : bitLength (limit - 1);
  }

  public static String javaToXML (String javaString)
  {
    StringBuilder stringBuilder = new StringBuilder ();

    for (int i = 0;i < javaString.length ();i++)
      {
        char c = javaString.charAt (i);
        boolean isUpperCase = Character.isUpperCase (c);
        if (i != 0 && isUpperCase)
          stringBuilder.append ('-');
        stringBuilder.append (isUpperCase ? Character.toLowerCase (c) : c);
      }
    
    return stringBuilder.toString ();
  }
  
  static Random random = new Random ();

  public static void randomSeed (long seed)
  {
    random = new Random (seed);
  }

  public static int random (int limit)
  {
    return (int) (limit * random.nextDouble ());
  }

  public static int randomIndex (Object [] arr)
  {
    return random (arr.length);
  }

  public static Object randomObject (Object [] arr)
  {
    return arr [randomIndex (arr)];
  }
  
  public static Object randomObject (List list)
  {
    return list.get (random (list.size ()));
  }

  public static float [] linearCombination (float [] coefficients,
                                            float [] [] vectors)
  {
    Assertions.expect (coefficients.length,vectors.length);
    if (vectors.length == 0)
      return null;
    float [] result = new float [vectors [0].length];
    for (int i = 0;i < coefficients.length;i++)
      {
        float [] vector = vectors [i];
        float coefficient = coefficients [i];
        Assertions.expect (vector.length,result.length);
        for (int j = 0;j < result.length;j++)
          result [j] += coefficient * vector [j];
      }
    return result;
  }

  public static int intValue (String value)
  {
    int base;
    int start;

    if (value.startsWith ("0x"))
      {
        start = 2;
        base = 16;
      }
    else if (value.startsWith ("\\"))
      {
        start = 1;
        base = 8;
      }
    else
      {
        start = 0;
        base = 10;
      }
    return Integer.parseInt (value.substring (start),base);
  }

  // Though these are meant to clip val between min and max,
  // they are coincidentally invariant with respect to
  // permutations of the arguments that preserve min < max;
  // they simply find the argument that lies in between the other two.
  public static double clip (double val,double min,double max)
  {
    return val < min ? min : val > max ? max : val;
  }

  public static float clip (float val,float min,float max)
  {
    return val < min ? min : val > max ? max : val;
  }

  public static int clip (int val,int min,int max)
  {
    return val < min ? min : val > max ? max : val;
  }

  public static float [] [] toMatrix (float [] array)
  {
    int dim = (int) Math.sqrt (array.length);
    Assertions.expect (dim*dim,array.length);
    float [] [] matrix = new float [dim] [dim];
    for (int i = 0,k = 0;i < dim;i++)
      for (int j = 0;j < dim;j++,k++)
        matrix [i] [j] = array [k];
    return matrix;
  }
  
  public static boolean matches (String string,String pattern)
  {
    if (string.length () != pattern.length ())
      return false;

    for (int i = 0;i < string.length ();i++)
      if (pattern.charAt (i) != '?' &&
          string.charAt (i) != pattern.charAt (i))
        return false;

    return true;
  }

  public static List rangeList (String ranges)
  {
    List rangeList = new ArrayList ();
    StringTokenizer tok = new StringTokenizer (ranges,",");
    while (tok.hasMoreTokens ())
    {
      String range = tok.nextToken ();
      int hyphen = range.indexOf ('-');
      if (hyphen == -1)
        rangeList.add (new Integer (range));
      else
      {
        int first = Integer.parseInt (range.substring (0,hyphen));
        int last  = Integer.parseInt (range.substring (hyphen + 1));
        for (int i = first;i <= last;i++)
          rangeList.add (new Integer (i));
      }
    }
    return rangeList;
  }
  
  public static double relativeDifference (double x1,double x2)
  {
    return x1 == x2 ? 0 : (x1 - x2) / Math.max (Math.abs (x1),Math.abs (x2));
  }
  
  public static String getHumanClassName (String className) {
    StringBuilder stringBuilder = new StringBuilder ();
    char [] name = className.toCharArray ();
    for (int i = 0;i < name.length;i++) {
      char c = name [i];
      if (!Character.isLowerCase (c) && (i + 1 == name.length || Character.isLowerCase (name [i + 1]))) {
        if (i != 0)
          stringBuilder.append (' '); 
        c = Character.toLowerCase (c);
      }
      stringBuilder.append (c);
 
    }
    return stringBuilder.toString ();
  }

  public static String getConstantName (String name) {
    return getHumanClassName (name).replace (' ','_').toUpperCase ();
  }
  
  public static String toMixedCase (String string) {
    boolean capitalize = false;
    StringBuilder stringBuilder = new StringBuilder ();
    for (char c : string.toCharArray ()) {
      if (c == ' ' || c == '_')
        capitalize = true;
      else {
        stringBuilder.append (capitalize ? Character.toUpperCase (c) : Character.toLowerCase (c));
        capitalize = false;
      }
    }
    return stringBuilder.toString ();
  }
  
  public static String toHyphenCase (String string) {
    char [] chars = string.toCharArray ();
    
    int i = 0;
    for (char c : chars)
      chars [i++] = c == ' ' || c == '_' ? '-' : Character.toLowerCase (c);
    
    return new String (chars);
  }

  public static String collapseMultipleSpaces (String string) {
    StringBuilder collapsedBuilder = new StringBuilder ();
    boolean wasSpace = false;
    for (char c : string.toCharArray ()) {
      boolean isSpace = c == ' ';
      if (!(wasSpace && isSpace))
        collapsedBuilder.append (c);
      wasSpace = isSpace;
    }
    return collapsedBuilder.toString ();
  }
}
