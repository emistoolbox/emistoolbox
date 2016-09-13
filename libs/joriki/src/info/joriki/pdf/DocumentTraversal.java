/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.io.IOException;

import java.util.List;
import java.util.ArrayList;

import info.joriki.util.Assertions;

import info.joriki.graphics.Rectangle;

public class DocumentTraversal
{
  PDFDocument document;
  PageHandler handler;
  PageLabeler labeler;
  int pageNumber;
  int firstPage;
  int lastPage;

  public void traversePages (PDFDocument document,PageHandler handler) throws IOException
  {
    traversePages (document,handler,0,Integer.MAX_VALUE);
  }

  public void traversePages (PDFDocument document,PageHandler handler,int firstPage,int lastPage) throws IOException
  {
    this.document = document;
    this.handler = handler;
    this.pageNumber = 0;
    this.firstPage = firstPage;
    this.lastPage = lastPage;
    this.labeler = new PageLabeler (document);
    handlePageObject (document.getPageRoot (),null,null,null,null,null);
    handler.finish ();
  }

  private boolean handlePageObject
    (final PDFDictionary pageObject,
     PDFDictionary expectedParent,
     Rectangle inheritedMediaBox,
     Rectangle inheritedCropBox,
     PDFInteger inheritedRotate,
     PDFDictionary inheritedResources)
    throws IOException
  {
    Assertions.expect (pageObject.get ("Parent"),expectedParent);
    PDFInteger rotate = (PDFInteger) pageObject.get ("Rotate",inheritedRotate);
    Rectangle mediaBox = pageObject.getRectangle ("MediaBox",inheritedMediaBox);
    Rectangle cropBox = pageObject.getRectangle ("CropBox",inheritedCropBox);
    PDFDictionary resources = (PDFDictionary) pageObject.get ("Resources",inheritedResources);

    if (pageObject.isOfType ("Pages")) // a branch
      {
        PDFArray kids = (PDFArray) pageObject.get ("Kids");
        int expectedPageNumber = pageNumber + pageObject.getInt ("Count");
        pageObject.checkUnused ("3.26");
        for (int i = 0;i < kids.size ();i++)
          if (handlePageObject ((PDFDictionary) kids.get (i),pageObject,mediaBox,cropBox,rotate,resources))
            return true;
        Assertions.expect (pageNumber,expectedPageNumber);
      }
    else // a leaf
      {
        pageNumber++;

        String pageLabel = labeler.nextLabel ();

        if (firstPage <= pageNumber && pageNumber <= lastPage)
          {
            Assertions.expect (pageObject.isOfType ("Page"));
            if (cropBox == null)
              cropBox = mediaBox;

            final List beadList = new ArrayList ();

            document.traverseThreads (new ThreadHandler () {
                public void handleThread (PDFDictionary thread)
                {
                  thread.use ("I");
                }

                public void handleBead (PDFBead bead)
                {
                  if (bead.pageObject == pageObject)
                    beadList.add (bead);
                }

                public void finishThread () {}
              });

            PDFArray annotationArray = (PDFArray) pageObject.get ("Annots");
            PDFAnnotation [] annotations = null;
            if (annotationArray != null)
              {
                annotations = new PDFAnnotation [annotationArray.size ()];
                for (int i = 0;i < annotations.length;i++)
                {
                  
                  PDFDictionary annotationDictionary = (PDFDictionary) annotationArray.get (i);
                  annotations [i] = new PDFAnnotation (annotationDictionary,document);
                  PDFDictionary annotationPage = (PDFDictionary) annotationDictionary.get ("P");
                  Assertions.expect (annotationPage == null || annotationPage == pageObject);
                }
              }

            PageInfo pageInfo = new PageInfo ();
            pageInfo.pageNumber = pageNumber;
            pageInfo.pageLabel = pageLabel;
            pageInfo.pageObject = pageObject;
            pageInfo.mediaBox = new Rectangle (mediaBox);
            pageInfo.cropBox = new Rectangle (cropBox);
            pageInfo.cropBox.intersectWith (mediaBox);
            pageInfo.resources = resources;
            pageInfo.beads = (PDFBead []) beadList.toArray (new PDFBead [0]);
            pageInfo.annotations = annotations;
            pageInfo.rotation = rotate == null ? 0 : rotate.rotationValue ();
            pageInfo.additionalActions = PDFUtil.getAdditionalActions (pageObject,document);

            StringBuilder messageBuilder = new StringBuilder ();
            messageBuilder.append ("handling page ").append (pageNumber);
            if (pageLabel != null)
              messageBuilder.append (" labeled " + pageLabel);
            System.err.println (messageBuilder);
            System.out.println (messageBuilder);

            if (handler.handle (pageInfo))
              return true;
          }
      }
    return false;
  }
}
