/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util;

public class Assertions
{
  private Assertions () {}

  final private static boolean debugging = true;

  public static void expect (boolean condition) throws AssertionException
  {
    if (debugging)
      if (!condition)
        throw new AssertionException ();
  }

  public static void unexpect (boolean condition) throws AssertionException
  {
    if (debugging)
      if (condition)
        throw new AssertionException ();
  }

  public static void expect (boolean condition,String message) throws AssertionException
  {
    if (debugging)
      if (!condition)
        throw new AssertionException (message);
  }

  public static void expect (boolean found,boolean expected) throws AssertionException
  {
    if (debugging)
      if (found != expected)
        throw new AssertionException ("expected " + expected + ", found " + found);
  }

  public static void expect (int found,int expected) throws AssertionException
  {
    if (debugging)
      if (found != expected)
        throw new AssertionException ("expected " + expected + ", found " + found);
  }

  public static void expect (int found,int expected,String description) throws AssertionException
  {
    if (debugging)
      if (found != expected)
        throw new AssertionException ("unexpected " + description + " : expected " + expected + ", found " + found);
  }

  public static void expect (int found,String foundDescription,int expected,String expectedDescription) throws AssertionException
  {
    if (debugging)
      if (found != expected)
        throw new AssertionException (found + " (" + foundDescription + ") should be " + expected + " (" + expectedDescription + ")");
  }

  public static void expect (double found,double expected) throws AssertionException
  {
    if (debugging)
      if (found != expected)
        throw new AssertionException ("expected " + expected + ", found " + found);
  }

  public static void expect (Object found,Object expected) throws AssertionException
  {
    if (debugging)
      if ((found == null && expected != null) ||
          (found != null && !found.equals (expected)))
        throw new AssertionException ("expected " + expected + ", found " + found);
  }

  public static void expect (Object found,Object expected,String description) throws AssertionException
  {
    if (debugging)
      if ((found == null && expected != null) ||
          (found != null && !found.equals (expected)))
        throw new AssertionException ("unexpected " + description + " : expected " + expected + ", found " + found);
  }

  public static void expect (Object found,String foundDescription,Object expected,String expectedDescription) throws AssertionException
  {
    if (debugging)
      if ((found == null && expected != null) ||
          (found != null && !found.equals (expected)))
        throw new AssertionException (found + " (" + foundDescription + ") should be " + expected + " (" + expectedDescription + ")");
  }

  public static void unexpect (boolean condition,String message) throws AssertionException
  {
    if (debugging)
      if (condition)
        throw new AssertionException (message);
  }

  public static void unexpect (boolean found,boolean unexpected) throws AssertionException
  {
    if (debugging)
      if (found == unexpected)
        throw new AssertionException ("unexpected " + found);
  }

  public static void unexpect (int found,int unexpected) throws AssertionException
  {
    if (debugging)
      if (found == unexpected)
        throw new AssertionException ("unexpected " + found);
  }

  public static void unexpect (double found,double unexpected) throws AssertionException
  {
    if (debugging)
      if (found == unexpected)
        throw new AssertionException ("unexpected " + found);
  }

  public static void unexpect (Object found,Object unexpected) throws AssertionException
  {
    if (debugging)
      if ((found == null && unexpected == null) ||
          (found != null && found.equals (unexpected)))
        throw new AssertionException ("unexpected " + found);
  }

  public static void limit (int found,int min,int max) throws AssertionException
  {
    if (debugging)
      if (!(min <= found && found <= max))
        throw new AssertionException (found + " out of range [" + min + "," + max + "]");
  }

  public static void limit (double found,double min,double max) throws AssertionException
  {
    if (debugging)
      if (!(min <= found && found <= max))
        throw new AssertionException (found + " out of range [" + min + "," + max + "]");
  }
}

