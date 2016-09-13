/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.util.Set;

public class PageInfo extends BasicPageInfo
{
  public int pageNumber;
  public String pageLabel;
  public PDFDictionary pageObject;
  public PDFAnnotation [] annotations;
  public PDFBead [] beads;
  public Set additionalActions;
}
