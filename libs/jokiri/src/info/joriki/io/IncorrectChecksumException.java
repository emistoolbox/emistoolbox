/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.IOException;

public class IncorrectChecksumException
  extends IOException
{
  int checksum;

  IncorrectChecksumException (int checksum)
  {
    this.checksum = checksum;
  }

  public int getChecksum ()
  {
    return checksum;
  }

  public ReadableInputStream getInputStream ()
  {
    return new ReadableInputStream () {
        int left = 4;
        public int read ()
        {
          return left == 0 ? -1 : (checksum >> (--left << 3)) & 0xff;
        }
      };
  }
}
