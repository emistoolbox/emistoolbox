/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ProtocolHandlers {
  private ProtocolHandlers () {}
  private static Map protocolHandlers = new HashMap ();

  public static void setProtocolHandler (String protocol,ProtocolHandler protocolHandler)
  {
    protocolHandlers.put (protocol,protocolHandler);
  }

  public static FullySeekable getFullySeekable (String string) throws IOException
  {
    URL url = new URL (string);
    ProtocolHandler protocolHandler = (ProtocolHandler) protocolHandlers.get (url.getProtocol());
    return protocolHandler == null ? null : protocolHandler.getFullySeekable (url);
  }
}
