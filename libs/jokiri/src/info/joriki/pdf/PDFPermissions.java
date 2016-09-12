/*
 * Copyright 2006 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

public interface PDFPermissions {
  int reservedMask = 0xffff0c3;
  int reservedBits = 0xffff0c0;
  
  int PRINT    = 1 << 2;
  int MODIFY   = 1 << 3;
  int COPY     = 1 << 4;
  int ANNOTATE = 1 << 5;
  int FILL     = 1 << 8;
  int EXTRACT  = 1 << 9;
  int ASSEMBLE = 1 << 10;
  int RESOLVE  = 1 << 11;
}
