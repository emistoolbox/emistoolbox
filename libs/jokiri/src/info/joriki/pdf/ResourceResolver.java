/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.util.Stack;
import java.util.Map;
import java.util.HashMap;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class ResourceResolver implements DeviceColorSpaces, ObjectTypes, PDFOptions
{
  Stack<PDFDictionary> resourceDictionaryStack = new Stack<PDFDictionary> ();
  Stack<PDFColorSpace []> defaultColorSpaceStack = new Stack<PDFColorSpace []> ();
  PDFDictionary pageResourceDictionary;
  PDFDictionary currentResourceDictionary;
  PDFColorSpace [] defaultColorSpaces;

  public void setPageResourceDictionary (PDFDictionary dictionary) {
    pageResourceDictionary = dictionary;
  }
  
  public void pushResourceDictionary (PDFDictionary dictionary)
  {
    if (dictionary == null)
      dictionary = pageResourceDictionary;
    resourceDictionaryStack.push (currentResourceDictionary);
    defaultColorSpaceStack.push (defaultColorSpaces);
    currentResourceDictionary = dictionary;
    defaultColorSpaces = new PDFColorSpace [3];

    for (int i = 0;i < deviceColorSpaces.length;i++)
      {
        PDFObject defaultColorSpaceSpecification = lookupResource
          ("ColorSpace",new PDFName ("Default" + deviceColorSpaceSuffixes [i]));
        defaultColorSpaces [i] = defaultColorSpaceSpecification != null ?
          getColorSpace (defaultColorSpaceSpecification) :
          deviceColorSpaces [i];
      }
  }

  public void popResourceDictionary ()
  {
    defaultColorSpaces = defaultColorSpaceStack.pop ();
    currentResourceDictionary = resourceDictionaryStack.pop ();
  }

  public PDFColorSpace resolveColorSpace (PDFObject key)
  {
    PDFColorSpace directColorSpace = getColorSpace (key);
    return directColorSpace != null ? directColorSpace :
      getColorSpace (resolveResource ("ColorSpace",key));
  }

  public PDFColorSpace getColorSpace (PDFObject specification)
  {
    if (specification instanceof PDFName)
      {
      	String name = ((PDFName) specification).getName ();
        return name.equals ("Pattern") ?
          new ColoredPatternColorSpace () :
          getDeviceColorSpace (name);
      }
    return (PDFColorSpace) getCachedObject (COLORSPACE,specification);
  }

  public PDFColorSpace getDeviceColorSpace (String name) {
    for (int i = 0;i < deviceColorSpaces.length;i++)
      if (name.equals ("Device" + deviceColorSpaceSuffixes [i]))
        return deviceColorSpaces [i];
    return null;
  }
  
  public PDFColorSpace map (PDFColorSpace colorSpace)
  {
    if (colorSpace == null)
      return null;
    if (colorSpace instanceof DeviceColorSpace && !approximateColors.isSet())
      return defaultColorSpaces [((DeviceColorSpace) colorSpace).which];
    if (colorSpace instanceof DerivedColorSpace)
      ((DerivedColorSpace) colorSpace).baseColorSpace.map (this);
    else if (colorSpace instanceof UncoloredPatternColorSpace)
      ((UncoloredPatternColorSpace) colorSpace).baseColorSpace.map (this);
    return colorSpace;
  }

  public PDFObject lookupResource (String resourceType,PDFName key)
  {
    PDFDictionary dict = (PDFDictionary) currentResourceDictionary.get (resourceType);
    return dict == null ? null : dict.get (key);
  }

  public PDFObject resolveResource (String resourceType,PDFObject key)
  {
    if (key instanceof PDFName)
      {
        PDFObject value = lookupResource (resourceType,(PDFName) key);
        if (value != null)
          return value;
      }
    return key;
  }

  public Object getCachedObject (int type,PDFObject specification)
  {
    return objectCache [type].getObject (specification);
  }

  // the indices here must correspond to the ones in ObjectTypes!
  String [] cachedResources = new String [] {
    "ColorSpace","Pattern","Shading","Font"
  };

  final static Class [] cachedClasses = new Class [] {
    PDFColorSpace.class,
    PDFPattern.class,
    PDFShading.class,
    PDFFont.class,
    PDFFunction.class
  };

  ObjectCache [] objectCache = new ObjectCache [cachedClasses.length];
  {
    for (int i = 0;i < objectCache.length;i++)
      objectCache [i] = new ObjectCache (cachedClasses [i]);
  }

  public Object getCachedResource (int type,PDFName name)
  {
    return getCachedObject (type,lookupResource (cachedResources [type],name));
  }

  final static Class [] parameterTypes = new Class []
  {PDFObject.class,ResourceResolver.class};

  class ObjectCache
  {
    Map cache = new HashMap ();
    
    Method getInstance;
    Object [] arguments = new Object [] {null,ResourceResolver.this};

    ObjectCache (Class classObject)
    {
      try {
        getInstance = classObject.getMethod ("getInstance",parameterTypes);
      } catch (NoSuchMethodException nsme) {
        nsme.printStackTrace ();
        throw new Error ("can't obtain instance creation method for " + classObject);
      }
    }

    Object getObject (PDFObject specification)
    {
      if (specification == null)
        return null;
      Object object = cache.get (specification);
      if (object == null)
        {
          // not cached -- construct a new object ...
          arguments [0] = specification;
          try {
            object = getInstance.invoke (null,arguments);
          } catch (IllegalAccessException iae) {
            iae.printStackTrace ();
            throw new Error ("can't access instance creation method");
          } catch (InvocationTargetException ite) {
            Throwable cause = ite.getTargetException ();
            if (cause instanceof RuntimeException)
              throw (RuntimeException) cause;
            if (cause instanceof Error)
              throw (Error) cause;
            cause.printStackTrace ();
            throw new Error ("Checked exception in instance creation method");
          }
          // ... and cache it
          cache.put (specification,object);
        }
      return object;
    }
  }
}
