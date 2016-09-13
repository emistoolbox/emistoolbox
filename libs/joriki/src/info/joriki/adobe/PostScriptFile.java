/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

// this is a watered-down PostScript parser that can
// be used to read Type 1 fonts, CMap files etc.

package info.joriki.adobe;

import java.io.IOException;
import java.io.InputStream;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Iterator;

import info.joriki.io.Resources;
import info.joriki.io.EmptyInputStream;
import info.joriki.io.HexadecimalInputStream;

import info.joriki.util.General;
import info.joriki.util.Options;
import info.joriki.util.Assertions;
import info.joriki.util.NotImplementedException;

import info.joriki.crypto.PostScriptCypher;
import info.joriki.crypto.DecypheringInputStream;

import info.joriki.graphics.Point;
import info.joriki.graphics.Transformation;

public class PostScriptFile
{
  public final static String resourceDirectory = "resources";
  public final static Map resources = new HashMap ();
  private final static Object save = new Object ();
  private final static Object mark = new Object ();
  private final static Object procedureCloser = new Object () {
      public String toString () { return "unbalanced }"; }
    };

  private final static String [] operators = {
    "]",">>","dict","begin","def","bind","where","exec","exit","loop","load","get","aload","astore","copy","add","definefont","defineresource","findfont","findresource","FontDirectory","save","restore","length","cvx","and","or","if","ifelse","known","for","repeat","closefile","mark","readonly","executeonly","noaccess","matrix","dtransform","eq","ne","dup","roll","end","StandardEncoding","currentdict","userdict","systemdict","currentfile","eexec","mul","sqrt","gt","not","array","index","exch","string","readstring","pop","put","debug","print","count","type","cvr","sub","neg","lt","div"};

  static interface ControlConstruct
  {
    boolean condition ();
    void perform ();
  }  

  protected AdobeStreamTokenizer tok;

  final public Stack operandStack = new Stack ();
  final public Stack executionStack = new Stack ();
  final public Stack dictionaryStack = new Stack ();

  private Map userDictionary = new HashMap ();
  private Map systemDictionary = new HashMap ();
  private Map globalDictionary = new HashMap ();
  
  private int dummyBytes;
  private boolean hexadecimal;
  private boolean postScriptStyle;
  
  Object readObject () throws IOException
  {
    switch (tok.nextToken ())
      {
      case AdobeStreamTokenizer.TT_EOF :
        return null;
      case '[' :
        return "mark";
      case '{' :
        Object object;
        Procedure procedure = new Procedure ();
        while ((object = readObject ()) != procedureCloser)
          procedure.add (object);
        return procedure;
      case ']' :
        return "]";
      case '}' :
        return procedureCloser;
      case AdobeStreamTokenizer.TT_OPEN :
        return "mark";
      case AdobeStreamTokenizer.TT_CLOSE :
        return ">>";
      case AdobeStreamTokenizer.TT_NAME :
        return new Name (new String (tok.bval));
      case AdobeStreamTokenizer.TT_STRING :
        return tok.bval;
      case AdobeStreamTokenizer.TT_INTEGER :
        return new Integer ((int) tok.nval);
      case AdobeStreamTokenizer.TT_REAL :
        return new Double (tok.nval);
      case AdobeStreamTokenizer.TT_WORD :
        String word = new String (tok.bval);
        if (word.equals ("true"))
          return new Boolean (true);
        if (word.equals ("false"))
          return new Boolean (false);
        return word;
      default : throw new IOException ("illegal token " + (char) tok.ttype);
      }
  }

  private final void executeProcedure (List procedure)
  {
    executionStack.push (procedure.iterator ());
  }

  protected final int getInt ()
  {
    return ((Integer) operandStack.pop ()).intValue ();
  }

  protected Object loadResource (String key,String category) throws IOException
  {
    new PostScriptFile (Resources.getInputStream (PostScriptFile.class,resourceDirectory + '/' + category + '/' + key));
    return lookupResource (key,category);
  }

  protected Map findResourceMap (String category)
  {
    return (Map) resources.get (category);
  }

  protected void pushResource (String category) throws IOException
  {
    operandStack.push (findResource (((Name) operandStack.pop ()).name,category));
  }

  protected Object lookupResource (String key,String category)
  {
    Map resourceMap = findResourceMap (category);
    return resourceMap == null ? null : resourceMap.get (key);
  }
    
  protected Object findResource (String key,String category) throws IOException
  {
    Object resource = lookupResource (key,category);
    return resource == null ? loadResource (key,category) : resource;
  }

  protected void defineResource (String category)
  {
    Object value = operandStack.pop ();
    String key = ((Name) operandStack.pop ()).name;
    defineResource (key,value,category);
    operandStack.push (value);
  }

  protected Map findOrCreateResourceMap (String category)
  {
    Map resourceMap = findResourceMap (category);
    if (resourceMap == null)
      {
        resourceMap = new HashMap ();
        resources.put (category,resourceMap);
      }
    return resourceMap;
  }

  protected void defineResource (String key,Object value,String category)
  {
    findOrCreateResourceMap (category).put (key,value);
  }

  // look up key in the dictionaries on the dictionary stack
  protected Object lookup (String key)
  {
    int i = dictionaryStack.size ();
    while (--i >= 0)
      {
        Object object = ((Map) dictionaryStack.get (i)).get (key);
        if (object != null)
          return object;
      }
    return null;
  }

  protected Map whereIs (String key)
  {
    int i = dictionaryStack.size ();
    while (--i >= 0)
    {
      Map dictionary = (Map) dictionaryStack.get (i);
      if (dictionary.containsKey (key))
        return dictionary;
    }
    return null;
  }

  public void setInputStream (InputStream in)
  {
    tok = new AdobeStreamTokenizer (in,postScriptStyle);
  }

  public PostScriptFile (InputStream in) throws IOException
  {
    this (in,true);
  }
  
  public PostScriptFile (InputStream in,boolean postScriptStyle) throws IOException
  {
    this (in,postScriptStyle,false,0);
  }

  public PostScriptFile (InputStream in,boolean postScriptStyle,boolean hexadecimal,int dummyBytes) throws IOException
  {
    this.dummyBytes = dummyBytes;
    this.hexadecimal = hexadecimal;
    this.postScriptStyle = postScriptStyle;
    
    for (byte i = 0;i < operators.length;i++)
      systemDictionary.put (operators [i],new Byte (i));

    dictionaryStack.push (systemDictionary);
    dictionaryStack.push (globalDictionary);
    dictionaryStack.push (userDictionary);

    setInputStream (in);

    try {
      skipDummyBytes (in);
      parse ();
    } finally { in.close (); }
  }

  protected void parse () throws IOException {
    Object object;
    while ((object = readObject ()) != null)
    {
      if (object instanceof String)
      {
        executionStack.push (object);
        execute ();
      }
      else
        operandStack.push (object);
    }
  }

  public void execute () throws IOException
  {
    while (!executionStack.isEmpty ())
    {
      Object object = executionStack.pop ();

      if (object instanceof Procedure)
        object = ((Procedure) object).iterator ();

      if (object instanceof Iterator)
      {
        Iterator iterator = (Iterator) object;
        if (!iterator.hasNext ())
          continue;
        object = iterator.next ();
        executionStack.push (iterator);
      }

      if (object instanceof ControlConstruct)
      {
        ControlConstruct cc = (ControlConstruct) object;
        if (cc.condition ())
        {
          executionStack.push (cc);
          cc.perform ();
        }
      }
      else if (object instanceof String)
      {
        String name = (String) object;
        if (Options.tracing)
          System.out.println ("looking up " + name);
        if (!handleSpecial (name))
        {
          object = lookup (name);
          if (object == null)
            throw new NotImplementedException ("operator " + name);
          executionStack.push (object);
        }
      }
      else if (object instanceof Byte)
      {
        List src,dst;
        Number n1,n2;
        Object o1,o2;
        int operator = ((Byte) object).intValue ();
        if (Options.tracing)
          System.out.println ("executing " + operators [operator]);
        switch (operator)
        {
        case 0: // ]
          operandStack.push (getList ());
          break;
        case 1: // >>
          Iterator iterator = getList ().iterator ();
          Map map = new HashMap ();
          while (iterator.hasNext ())
          {
            Object key = iterator.next ();
            if ("def".equals (key))
              // this occurs in a ToUnicode map in tsboat-acc.pdf
              Options.warn ("Spurious def in dictionary");
            else
              map.put (((Name) key).name,iterator.next ());
          }
          operandStack.push (map);
          break;
        case 2: // dict
          Assertions.expect (operandStack.pop () instanceof Integer);
          operandStack.push (new HashMap ());
          break;
        case 3: // begin
          dictionaryStack.push (operandStack.pop ());
          break;
        case 4: // def
          Object value = operandStack.pop ();
          Name name = (Name) operandStack.pop ();
          define (name.name,value);
          break;
        case 5: // bind
          bind ((List) operandStack.peek ());
          break;
        case 6: // where
          Map dictionary = whereIs (((Name) operandStack.pop ()).name);
          boolean found = dictionary != null;
          if (found)
            operandStack.push (dictionary);
          operandStack.push (new Boolean (found));
          break;
        case 7: // exec
          executeProcedure ((Procedure) operandStack.pop ());
          break;
        case 8: // exit
          while (!(executionStack.pop () instanceof ControlConstruct))
            ;
          break;
        case 9: // loop
          executionStack.push (new ControlConstruct () {
            List procedure = (List) operandStack.pop ();
            public boolean condition ()
            {
              return true;
            }
            public void perform ()
            {
              executeProcedure (procedure);
            }
          });
          break;
        case 10: // load
          operandStack.push (lookup (((Name) operandStack.pop ()).name));
          break;
        case 11: // get
          Object which = operandStack.pop ();
          Object source = operandStack.pop ();

          if (source instanceof Map)
            operandStack.push (((Map) source).get
                (((Name) which).name));
          else if (source instanceof List)
            operandStack.push (((List) source).get
                (((Integer) which).intValue ()));
          else
            throw new NotImplementedException
            ("get from source " + source.getClass ());
          break;
        case 12: // aload
          src = (List) operandStack.pop ();
          for (int i = 0;i < src.size ();i++)
            operandStack.push (src.get (i));
          operandStack.push (src);
          break;
        case 13: // astore
          dst = (List) operandStack.pop ();
          for (int i = 0;i < dst.size ();i++)
            dst.set (i,operandStack.pop ());
          operandStack.push (dst);
          break;
        case 14: // copy
          Object top = operandStack.pop ();
          if (top instanceof Integer)
          {
            int n = ((Integer) top).intValue ();
            int len = operandStack.size ();
            for (int i = len - n;i < len;i++)
              operandStack.push (operandStack.elementAt (i));
          }
          else
          {
            Object bot = operandStack.pop ();
            if (bot instanceof List)
            {
              src = (List) bot;
              dst = (List) top;
              for (int i = 0;i < src.size ();i++)
                dst.set (i,src.get (i));
              operandStack.push
              (dst.size () == src.size () ? dst : dst.subList (0,src.size ()));
            }
            else
              throw new NotImplementedException ("copy for " + bot.getClass ());
          }
          break;
        case 15: // add
          n2 = (Number) operandStack.pop ();
          n1 = (Number) operandStack.pop ();
          pushArithmeticResult (n1,n2,n1.doubleValue () + n2.doubleValue ());
          break;
        case 16: // definefont
          defineResource ("Font");
          break;
        case 17: // defineresource
          defineResource (((Name) operandStack.pop ()).name);
          break;
        case 18: // findfont
          pushResource ("Font");
          break;
        case 19: // findresource
          pushResource (((Name) operandStack.pop ()).name);
          break;
        case 20: // FontDirectory
          operandStack.push (findOrCreateResourceMap ("Font"));
          break;
        case 21: // save
          operandStack.push (save);
          break;
        case 22: // restore
          if (operandStack.pop () != save)
            throw new Error ();
          break;
        case 23: // length
          object = operandStack.pop ();
          if (object instanceof List)
            operandStack.push (new Integer (((List) object).size ()));
          else
            throw new NotImplementedException ("length for " + object.getClass ());
          break;
        case 24 : // cvx
          object = operandStack.pop ();
          if (object instanceof Name)
            operandStack.push (((Name) object).name);
          else if (object instanceof List)
          {
            Procedure procedure = new Procedure ();
            procedure.addAll ((List) object);
            operandStack.push (procedure);
          }
          else
            throw new NotImplementedException ("conversion of " + object.getClass () + " to executable");
          break;
        case 25: // and
          operandStack.push
          (new Boolean
           (((Boolean) operandStack.pop ()).booleanValue () &&
            ((Boolean) operandStack.pop ()).booleanValue ()));
          break;
        case 26: // or
          operandStack.push
          (new Boolean
           (((Boolean) operandStack.pop ()).booleanValue () ||
            ((Boolean) operandStack.pop ()).booleanValue ()));
          break;
        case 27: // if
          Procedure procedure = (Procedure) operandStack.pop ();
          Boolean condition = (Boolean) operandStack.pop ();
          if (condition.booleanValue ())
            executeProcedure (procedure);
          break;
        case 28: // ifelse
          Procedure proc2 = (Procedure) operandStack.pop ();
          Procedure proc1 = (Procedure) operandStack.pop ();
          Boolean decision = (Boolean) operandStack.pop ();
          executeProcedure (decision.booleanValue () ? proc1 : proc2);
          break;
        case 29: // known
          Name key = (Name) operandStack.pop ();
          Map dict = (Map) operandStack.pop ();
          operandStack.push (new Boolean (dict.get (key.name) != null));
          break;
        case 30: // for
          executionStack.push (new ControlConstruct () {
            List procedure = (List) operandStack.pop ();
            Number n3 = (Number) operandStack.pop ();
            Number n2 = (Number) operandStack.pop ();
            Number n1 = (Number) operandStack.pop ();
            boolean allInts = 
              n1 instanceof Integer &&
              n2 instanceof Integer &&
              n3 instanceof Integer;

            double val = n1.doubleValue ();
            double increment = n2.doubleValue ();
            double limit = n3.doubleValue ();

            public boolean condition ()
            {
              return increment < 0 ? val >= limit : val <= limit;
            }

            public void perform ()
            {
              operandStack.push
              (allInts ?
               (Number) new Integer ((int) val) : 
               (Number) new Double (val));
              executeProcedure (procedure);
              val += increment;
            }
          });
          break;
        case 31: // repeat
          executionStack.push (new ControlConstruct () {
            List procedure = (List) operandStack.pop ();
            int n = getInt ();
            public boolean condition ()
            {
              return n-- > 0;
            }
            public void perform ()
            {
              executeProcedure (procedure);
            }
          });
          break;
        case 32: // closefile
          setInputStream (new EmptyInputStream ());
          break;
        case 33: // mark
          operandStack.push (mark);
          break;
        case 34: // readonly
        case 35: // executeonly
        case 36: // noaccess
          break;
        case 37: // matrix
          List matrix = new ArrayList ();
          for (int i = 0;i < 6;i++)
            matrix.add (new Integer (i == 0 || i == 3 ? 1 : 0));
          operandStack.push (matrix);
          break;
        case 38: // dtransform
          List transform = (List) operandStack.pop ();
          Number dy = (Number) operandStack.pop ();
          Number dx = (Number) operandStack.pop ();
          Assertions.expect (transform.size (),6);
          double [] arr = new double [6];
          for (int i = 0;i < 6;i++)
            arr [i] = ((Number) transform.get (i)).doubleValue ();
          Point p = new Point (dx.doubleValue (),
              dy.doubleValue ());
          p.transformBy (new Transformation (arr).linearPart ());
          operandStack.push (new Double (p.x));
          operandStack.push (new Double (p.y));
          break;
        case 39: // eq
          o2 = operandStack.pop ();
          o1 = operandStack.pop ();
          operandStack.push (new Boolean 
              (o1 instanceof Number && o2 instanceof Number ?
               o1.equals (o2) : o1 == o2));
          break;
        case 40: // ne
          o2 = operandStack.pop ();
          o1 = operandStack.pop ();
          operandStack.push (new Boolean 
              ((o1 instanceof Number && o2 instanceof Number) ||
               (o1 instanceof Name && o2 instanceof Name) ?
               !o1.equals (o2) : o1 != o2));
          break;
        case 41: // dup
          object = operandStack.pop ();
          operandStack.push (object);
          operandStack.push (object);
          break;
        case 42: // roll
          int j = ((Integer) operandStack.pop ()).intValue ();
          int n = ((Integer) operandStack.pop ()).intValue ();
          Object [] array = new Object [n];

          j = (j % n) + n; // make sure j is positive

          for (int i = 0;i < n;i++)
            array [i] = operandStack.pop ();

          for (int i = n - 1;i >= 0;i--)
            operandStack.push (array [(i + j) % n]);
          break;
        case 43: // end
          dictionaryStack.pop ();
          Assertions.expect (dictionaryStack.size () >= 3);
          break;
        case 44: // StandardEncoding
          operandStack.push (new Name ("StandardEncoding"));
          break;
        case 45: // currentdict
          operandStack.push (dictionaryStack.peek ());
          break;
        case 46: // userdict
          operandStack.push (userDictionary);
          break;
        case 47: // systemdict
          operandStack.push (systemDictionary);
          break;
        case 48: // currentfile
          operandStack.push (tok.in);
          break;
        case 49: // eexec
          InputStream in = (InputStream) operandStack.pop ();
          skipDummyBytes (in);
          in = new DecypheringInputStream
          (hexadecimal ? new HexadecimalInputStream (in) : in,
              new PostScriptCypher (PostScriptCypher.EEXEC));
          for (int i = 0;i < 4;i++)
            in.read ();
          setInputStream (in);
          break;
        case 50: // mul
          n2 = (Number) operandStack.pop ();
          n1 = (Number) operandStack.pop ();
          pushArithmeticResult (n1,n2,n1.doubleValue () * n2.doubleValue ());
          break;
        case 51: // sqrt
          operandStack.push
          (new Double (Math.sqrt (((Number) operandStack.pop ()).doubleValue ())));
          break;
        case 52: // gt
          n2 = (Number) operandStack.pop ();
          n1 = (Number) operandStack.pop ();
          operandStack.push (new Boolean (n1.doubleValue () > n2.doubleValue ()));
          break;
        case 53: // not
          operandStack.push (new Boolean
              (!((Boolean) operandStack.pop ()).booleanValue ()));
          break;
        case 54: // array
          operandStack.push (General.nullList (getInt ()));
          break;
        case 55: // index
          int index = getInt ();
          operandStack.push
          (operandStack.elementAt
           (operandStack.size () - (index + 1)));
          break;
        case 56: // exch
          o1 = operandStack.pop ();
          o2 = operandStack.pop ();
          operandStack.push (o1);
          operandStack.push (o2);
          break;
        case 57: // string
          operandStack.push (new byte [getInt ()]);
          break;
        case 58: // readstring
          byte [] str = (byte []) operandStack.pop ();
          InputStream stream = (InputStream) operandStack.pop ();
          operandStack.push (str);
          operandStack.push
          (new Boolean (stream.read (str) == str.length));
          break;
        case 59: // pop
          operandStack.pop ();
          break;
        case 60: // put
          Object what = operandStack.pop ();
          Object where = operandStack.pop ();
          Object destination = operandStack.pop ();

          if (destination instanceof List)
            ((List) destination).set
            (((Integer) where).intValue (),what);
          else if (destination instanceof Map)
            ((Map) destination).put
            (((Name) where).name,what);
          else
            throw new NotImplementedException
            ("put into destination " + destination.getClass ());
          break;
        case 61: // debug
          System.out.println (operandStack);
          break;
        case 62: // print
          System.out.println (new String ((byte []) operandStack.pop ()));
          break;
        case 63: // count
          operandStack.push (new Integer (operandStack.size ()));
          break;
        case 64: // type
          object = operandStack.pop ();
          if (object instanceof Integer)
            operandStack.push (new Name ("integertype"));
          else
            throw new NotImplementedException ("type for " + object.getClass ());
          break;
        case 65: // cvr
          object = operandStack.pop ();
          if (object instanceof Number)
            operandStack.push (new Double (((Number) object).doubleValue ()));
          else
            throw new NotImplementedException ("conversion of " + object.getClass () + " to real");
          break;
        case 66: // sub
          n2 = (Number) operandStack.pop ();
          n1 = (Number) operandStack.pop ();
          pushArithmeticResult (n1,n2,n1.doubleValue () - n2.doubleValue ());
          break;
        case 67: // neg
          n1 = (Number) operandStack.pop ();
          pushArithmeticResult (n1 instanceof Integer,-n1.doubleValue ());
          break;
        case 68: // lt
          n2 = (Number) operandStack.pop ();
          n1 = (Number) operandStack.pop ();
          operandStack.push (new Boolean (n1.doubleValue () < n2.doubleValue ()));
          break;
        case 69: // div
          n2 = (Number) operandStack.pop ();
          n1 = (Number) operandStack.pop ();
          operandStack.push (new Double (n1.doubleValue () / n2.doubleValue ()));
          break;
        default:
          throw new InternalError ();
        }
      }
      else
        operandStack.push (object);
    }
  }

  void pushArithmeticResult (Number n1,Number n2,double result) {
    pushArithmeticResult (n1 instanceof Integer && n2 instanceof Integer,result);
  }
  
  void pushArithmeticResult (boolean integer,double result) {
    int integerResult = (int) result;
    operandStack.push
    (integer && integerResult == result?
     (Number) new Integer (integerResult) :
     (Number) new Double (result));
  }
  
  void skipDummyBytes (InputStream in) throws IOException
  {
    for (int i = 0;i < dummyBytes;i++)
      in.read ();
  }
  
  void bind (List procedure)
  {
    for (int i = 0;i < procedure.size ();i++)
    {
      Object o = procedure.get (i);
      if (o instanceof List)
        bind ((List) o);
      else if (o instanceof String)
      {
        o = lookup ((String) o);
        if (o instanceof Byte)
          procedure.set (i,o);
      }
    }
  }
  
  protected void define (String key,Object value)
  {
    ((Map) dictionaryStack.peek ()).put (key,value);
  }
  
  List getList ()
  {
    int i = operandStack.size ();
    while (operandStack.get (--i) != mark)
      if (i == 0)
        throw new Error ("missed the mark");
    List list = new ArrayList ();
    while (++i < operandStack.size ())
      list.add (operandStack.get (i));
    for (i = list.size ();i >= 0;i--)
      operandStack.pop ();
    return list;
  }
  
  // subclasses like CMapFile can implement any special
  // operators here; a return value of true indicates that
  // the operator shouldn't be processed in the normal way
  // (in particular, that no exception should be thrown
  // if it is normally undefined).
  protected boolean handleSpecial (String operator) throws IOException
  {
    return false;
  }
}
