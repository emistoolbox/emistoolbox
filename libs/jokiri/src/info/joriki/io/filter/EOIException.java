/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io.filter;

import info.joriki.util.Options;

import java.io.EOFException;
import java.io.StringWriter;
import java.io.PrintWriter;

public class EOIException extends EOFException
{
  public void throwAt (String ... catchers) throws EOIException
  {
    StringWriter stringWriter = new StringWriter ();
    printStackTrace (new PrintWriter (stringWriter));
    for (String catcher : catchers)
      if (stringWriter.toString ().indexOf (catcher) != -1)
        throw this;
    Options.warn ("no catcher for EOI exception");
  }
}
