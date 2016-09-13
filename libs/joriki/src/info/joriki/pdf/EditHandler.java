/*
 * Copyright 2003 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.awt.Label;
import java.awt.TextField;

import info.joriki.util.Handler;

interface EditHandler
{
  Label createLabel (PDFObject original,String member,Handler<PDFObject> setter);
  TextField edit (PDFValue atom,String title);
  String getTitle ();
}
