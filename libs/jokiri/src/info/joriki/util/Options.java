/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util;

import info.joriki.io.Resources;
import info.joriki.io.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

public class Options {
  public static boolean tracing = false;
  final static Switch warn = new Switch ("issue warnings");
  final static ThreadLocalSet warnings = new ThreadLocalSet ();
  
  Class options;
  Object object;
  String syntax;
  
  String description;

  // static fields only
  public Options (Class options,String syntax,String description)
  {
    this.options = options;
    this.syntax = syntax;
    this.description = description;
  }
  
  public Options (Object object,String syntax,String description)
  {
    this (object.getClass (),syntax,description);
    this.object = object;
  }

  public ArgumentIterator parse (String [] args) throws IOException
  {
    return parse (args,null,null);
  }

  public ArgumentIterator parse (String [] args,String [] aliases) throws IOException
  {
    return parse (args,aliases,null);
  }
  
  public ArgumentIterator parse (String [] args,OptionHandler handler) throws IOException
  {
    return parse (args,null,handler);
  }
  
  public ArgumentIterator parse (String [] args,String [] aliases,OptionHandler handler) throws IOException
  {
    printCommandLineAndVersion (options,args);
    ArgumentIterator arguments = new ArgumentIterator (args);
    
    while (arguments.atOption ())
      try {
        String argument = arguments.nextString ();
        if (argument.length () == 2)
        {
          char option = argument.charAt (1);
          switch (option) {
          case 'W' : warn.set (); continue;
          case 'I' : System.setIn (new FileInputStream (arguments.nextString ())); continue;
          case 'O' : System.setOut (new PrintStream (new FileOutputStream (arguments.nextString ()))); continue;
          case 'E' : System.setErr (new PrintStream (new FileOutputStream (arguments.nextString ()))); continue;
          default :
            if (aliases != null && aliases [option] != null)
              argument = aliases [option];
            else
            {
              if (handler == null || !handler.handle (option,arguments))
                throw new Error ("undefined option " + argument);
              continue;
            }
          }
        }
        else if (argument.startsWith ("--"))
          argument = argument.substring (2);
        else
          throw new Error ("undefined option " + argument);

        if (argument.equals ("help") || argument.equals ("wikiHelp"))
        {  
          printUsage (argument.equals ("wikiHelp"));
          System.exit (0);
        }
        
        try {
          Object option = options.getField (argument).get (object);
          if (!(option instanceof Option))
            throw new Error (argument + " is not an option");
          if (option instanceof Switch)
            ((Switch) option).set ();
          else if (option instanceof Parameter)
            try {
              ((Parameter) option).set (arguments.nextString ());
            } catch (NoSuchElementException e) {
              throw new Error ("missing parameter for option " + argument);
            }
        } catch (NoSuchFieldException e) {
          throw new Error (argument + " is not an option");
        } catch (IllegalAccessException e) {
          e.printStackTrace();
          throw new Error ("can't access option " + argument);
        }
      } catch (Error e) {
        printUsage (false);
        throw e;
      }
    return arguments;
  }
  
  public static void printCommandLineAndVersion (Class mainClass,String [] args) throws IOException {
    StringBuilder commandLineBuilder = new StringBuilder ();
    commandLineBuilder.append ("Command line: java ").append (mainClass.getName ());
    for (String arg : args)
      {
        boolean needsQuotes = arg.length () == 0 || arg.indexOf (' ') != -1;
        commandLineBuilder.append (' ');
        if (needsQuotes)
          commandLineBuilder.append ('"');
        commandLineBuilder.append (arg);
        if (needsQuotes)
          commandLineBuilder.append ('"');
      }
    System.err.println (commandLineBuilder);
    System.err.print ("Version: ");
    Util.copy (Resources.getInputStream (Options.class,"version.txt"),System.err);
  }

  Set printed;
  
  public void printUsage (boolean forWiki) {
    StringBuilder usageBuilder = new StringBuilder ();
    if (description != null) {
      String name = options.getSimpleName ();
      if (Character.isUpperCase (name.charAt (name.length () - 1)))
        usageBuilder.append (name);
      else
        usageBuilder.append ("The ").append (General.getHumanClassName (name));
      usageBuilder.append (' ').append (description).append (".\n\n");
    }
    if (forWiki)
      usageBuilder.append ("<verbatim>\n");
    if (syntax != null)
      usageBuilder.append ("usage : java ").append (options.getName ()).append (" [options] ").append (syntax).append ("\n\n");
    printed = new HashSet ();
    appendUsage (options,usageBuilder);
    if (forWiki)
      usageBuilder.append ("</verbatim>\n");
    String usage = usageBuilder.toString ();
    System.err.println ();
    System.err.print (usage);
    if (forWiki)
      new SimpleClipboard ().setClipboardContents (usage);
  }
  
  private void appendUsage (Class options,StringBuilder buffer) {
    if (!(Modifier.isPublic (options.getModifiers()) && printed.add (options)))
      return;
    Field [] fields = options.getDeclaredFields();
    int width = 0;
    List usages = new ArrayList ();
    List optionDescriptions = new ArrayList ();
    try {
      for (int i = 0;i < fields.length;i++)
      {
        Field field = fields [i];
        if (!Modifier.isPublic (field.getModifiers()))
          continue;
        Object option = field.get(object);
        if (!(option instanceof Option))
          continue;
        String usage = field.getName();
        String optionDescription = ((Option) option).description; 
        if (option instanceof Parameter)
        {
          Parameter parameter = (Parameter) option;
          String parameterName = '<' + parameter.getParameterName() + '>';
          optionDescription = optionDescription.replaceAll("\\$",parameterName);
          usage += ' ' + parameterName;
          if (parameter instanceof FermiParameter)
            usage += " [" + ((FermiParameter) parameter).defaultValue + ']';
        }
        width = Math.max (width,usage.length());
        usages.add (usage);
        optionDescriptions.add (optionDescription);
      }
      if (usages.size () > 0)
      {
        buffer.append ("options declared in ").append (options.getName ()).append ('\n');
        for (int i = 0;i < usages.size ();i++)
        {
          buffer.append ("--").append (usages.get (i));
          for (int j = ((String) usages.get (i)).length ();j <= width;j++)
            buffer.append (' ');
          buffer.append (": ").append (optionDescriptions.get (i)).append ('\n');
        }
        buffer.append ('\n');
      }
    } catch (IllegalAccessException iae) {
      iae.printStackTrace ();
    }
    Class superClass = options.getSuperclass();
    if (superClass != null)
      appendUsage (superClass,buffer);
    Class[] interfaces = options.getInterfaces();
    for (int i = 0;i < interfaces.length;i++)
      appendUsage (interfaces [i],buffer);
  }
  
  public void reset ()
  {
    Field[] fields = options.getFields();
    for (int i = 0;i < fields.length;i++)
      try {
        Object option = fields [i].get (object);
        if (option instanceof Option)
          ((Option) option).reset ();
      } catch (IllegalAccessException iae) {
        iae.printStackTrace();
      }
  }
  
  public static File getInputFile (String [] args)
  {
    if (args.length == 0)
      return null;
    File inputFile = new File (args [args.length - 1]);
    return inputFile.exists () ? inputFile : null;
  }
  
  public static void warn (String message)
  {
    if (warn.isSet ())
      if (warnings.add(message))
        System.err.println ("Warning : " + message);
  }
  
  public static void warn (int level,String message)
  {
    if (warn.isSet ())
      if (warnings.add(new Pair (new Integer (level),message)))
        System.err.println ("Alert level " + level + " : " + message);
  }
  
  public static void main (String [] args) throws ClassNotFoundException {
    new Options (Class.forName (args [0]),null,null).printUsage (false);
  }
}
