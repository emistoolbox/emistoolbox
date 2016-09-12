/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import info.joriki.rdf.RDFGraph;
import info.joriki.util.Assertions;
import info.joriki.util.Cinderella;
import info.joriki.util.Count;
import info.joriki.util.Handler;
import info.joriki.util.NotImplementedException;
import info.joriki.util.Options;
import info.joriki.util.Pair;
import info.joriki.util.Version;
import info.joriki.xml.XMLUtil;

public class PDFDocument implements OptionalContent
{
  PDFDictionary root;
  PDFDictionary info;
  Version version;
  PDFArray id;

  public PDFDocument (PDFDictionary root,Version version)
  {
    this (root,null,version);
  }

  public PDFDocument (PDFDictionary root,PDFDictionary info,Version version)
  {
    this (root,info,version,null);
  }
  
  public PDFDocument (PDFDictionary root,PDFDictionary info,Version version,PDFArray id)
  {
    this.root = root;
    this.info = info;
    this.version = version != null ? version : new Version (1,4);
    this.id = id;
    root.unimplement ("AA");
    // ZA0506_0084.PDF
    root.ignore ("OutputIntents");
    
    setOptionalContentStates ();
  }

  public PDFDictionary getRoot ()
  {
    return root;
  }

  public PDFDictionary getInfo ()
  {
    return info;
  }

  public String getInfoString (String key)
  {
    return info == null ? null : info.getTextString (key);
  }
  
  public void setInfoString (String key,String value) {
    if (info == null)
      info = new PDFDictionary ();
    info.put (key,new PDFString (value));
  }

  PDFDictionary getPageRoot ()
  {
    return (PDFDictionary) root.get ("Pages");
  }

  public int getPageCount ()
  {
    return getPageRoot ().getInt ("Count");
  }

  public PDFArray getDefaultMediaBox ()
  {
    return (PDFArray) getPageRoot ().get ("MediaBox");
  }

  public PDFArray getPageArray ()
  {
    return (PDFArray) ((PDFDictionary) root.get ("Pages")).get ("Kids");
  }

  public PDFDictionary getNamesEntry (String key)
  {
    PDFDictionary names = (PDFDictionary) root.get ("Names");
    return names == null ? null : (PDFDictionary) names.get (key);
  }

  public PDFDictionary getPage (int pageNumber)
  {
    return getPageRoot ().getPage (new Count (pageNumber));
  }

  public Version getVersion ()
  {
    return version;
  }

  public void copyGlobalsFrom (PDFDocument document)
  {
    PDFDictionary destinationTree = document.getNamesEntry ("Dests");
    if (destinationTree != null)
      root.getOrCreateDictionary ("Names").
        putIndirect ("Dests",destinationTree);
    PDFDictionary destinationDictionary = (PDFDictionary) document.getRoot ().get ("Dests");
    if (destinationDictionary != null)
      root.putIndirect ("Dests",destinationDictionary);
    PDFDictionary acroFormDictionary = (PDFDictionary) document.getRoot ().get ("AcroForm");
    if (acroFormDictionary != null)
      root.putIndirect ("AcroForm",acroFormDictionary);
    PDFStream metadataStream = (PDFStream) document.getRoot ().get ("Metadata");
    if (metadataStream != null)
      root.putIndirect ("Metadata",metadataStream);
    PDFDictionary optionalContentPropertiesDictionary = (PDFDictionary) document.getRoot ().get ("OCProperties");
    if (optionalContentPropertiesDictionary != null)
      root.putIndirect ("OCProperties",optionalContentPropertiesDictionary);
    
    info = document.info;
  }

  private void setOptionalContentStates () {
    PDFDictionary optionalContentProperties = (PDFDictionary) root.get ("OCProperties");
    if (optionalContentProperties == null)
      return;
    PDFArray optionalContentGroups = (PDFArray) optionalContentProperties.get ("OCGs");
    PDFDictionary defaultConfiguration = (PDFDictionary) optionalContentProperties.get ("D");
    
    // workaround for missing default configuration
    // (web-2.0-for-the-enterprise.pdf) -- see 
    // OptionalContentVisibilityPolicy.getVisibility for why
    // it's probably a good idea not to set any optional
    // content states if this required entry is absent.
    if (defaultConfiguration == null)
      return;
    Assertions.expect (defaultConfiguration.get ("Intent",VIEW),VIEW);
    setOptionalContentStates (optionalContentGroups,defaultConfiguration);
    optionalContentProperties.checkUnused ("4.47");
  }
  
  private void setOptionalContentStates (PDFArray optionalContentGroups,PDFDictionary configurationDictionary) {
    String baseStateString = configurationDictionary.getName ("BaseState",ON);
    boolean baseState;
    if (baseStateString.equals (ON))
      baseState = true;
    else if (baseStateString.equals (OFF))
      baseState = false;
    else
      throw new NotImplementedException ("optional content base state " + baseStateString);
    setOptionalContentStates (optionalContentGroups,baseState);
    setOptionalContentStates ((PDFArray) configurationDictionary.get ("ON"),true);
    setOptionalContentStates ((PDFArray) configurationDictionary.get ("OFF"),false);
    // NB: the default value for both of the following is an empty array for the default
    // configuration, but it is the value in the default configuration for all other
    // configurations. As long as we don't allow non-empty values, this distinction is
    // not important.
    PDFArray radioButtonGroups = (PDFArray) configurationDictionary.get ("RBGroups");
    if (radioButtonGroups != null && !radioButtonGroups.isEmpty())
      throw new NotImplementedException ("radio button groups for optional content");
    PDFArray presentation = (PDFArray) configurationDictionary.get ("Order");
    if (presentation != null && !presentation.isEmpty())
      Options.warn ("user interface presentation of optional content");
    configurationDictionary.checkUnused ("4.48");
  }
  
  private void setOptionalContentStates (PDFArray optionalContentGroups,boolean state)
  {
    if (optionalContentGroups != null)
    {
      PDFBoolean stateObject = new PDFBoolean (state);
      for (int i = 0;i < optionalContentGroups.size ();i++)
      {
        PDFDictionary groupDictionary = (PDFDictionary) optionalContentGroups.get (i);
        Assertions.expect (groupDictionary.isOfType ("OCG"));
        // This is only informational as long as no groups are exposed via the Order entry
        // in the optional content configuration dictionary.
        groupDictionary.use ("Name");
        // This is only informational as long as there is no AS entry
        // in the optional content configuration dictionary.
        groupDictionary.use ("Usage");
        PDFObject intent = groupDictionary.get ("Intent",VIEW);
        if (intent instanceof PDFName)
          intent = new PDFArray (intent);
        // we only ever use the default configuration, whose intent must be View
        Assertions.expect (((PDFArray) intent).elements ().contains (VIEW));

        groupDictionary.checkUnused ("4.45");
        states.put (groupDictionary,stateObject);
      }
    }
  }

  public void traverseThreads (ThreadHandler handler)
  {
    PDFArray threads = (PDFArray) root.get ("Threads");
    if (threads != null)
      for (int threadNumber = 0;threadNumber < threads.size ();)
      {
        PDFDictionary thread = (PDFDictionary) threads.get (threadNumber);
        Assertions.expect (thread.isOptionallyOfType ("Thread"));
        handler.handleThread (thread);
        PDFDictionary first = (PDFDictionary) thread.get ("F");
        thread.checkUnused ("8.7");
        PDFDictionary bead = first;
        threadNumber++;
        int beadNumber = 0;
        do
          handler.handleBead (new PDFBead (bead,threadNumber,++beadNumber));
        while ((bead = (PDFDictionary) bead.get ("N")) != first);
        handler.finishThread ();
      }
  }
  
  public void traversePageObjects (Handler<PDFDictionary> handler) {
    getPageRoot ().traversePageObjects (handler);
  }
  
  public void traversePageNodes (Handler<PDFDictionary> handler) {
    getPageRoot ().traversePageNodes (handler);
  }
  
  public void traverseAnnotations (final Cinderella<PDFDictionary> cinderella) {
    traversePageObjects (new Handler<PDFDictionary> () {
      public void handle (PDFDictionary pageObject) {
        PDFArray annotations = (PDFArray) pageObject.get ("Annots");
        if (annotations != null) {
          Iterator iterator = annotations.iterator ();
          while (iterator.hasNext ())
            if (!cinderella.isGood ((PDFDictionary) iterator.next ()))
              iterator.remove ();
        }
      }
    });
  }
  
  public void traverseOutline (Cinderella<PDFDictionary> cinderella) {
    PDFDictionary outlines = (PDFDictionary) root.get ("Outlines");
    if (outlines != null)
      outlines.traverseOutline (cinderella);
  }
  
  public void traverseFormFields (Handler<PDFDictionary> handler) {
    PDFDictionary forms = (PDFDictionary) root.get ("AcroForm");
    if (forms != null)
      ((PDFArray) forms.get ("Fields")).traverseFormFields (handler);
  }
  
  public void traverseObjects (boolean resolve,Handler<PDFObject> handler) {
	    getRoot ().traverse (handler,resolve);
	  }

  public void traverseResources (final Handler<PDFDictionary> handler) {
    final Handler<PDFDictionary> resourceContainerHandler = new Handler<PDFDictionary> () {
          public void handle (PDFDictionary resourceContainer) {
            PDFDictionary resources = (PDFDictionary) resourceContainer.get ("Resources");
            if (resources != null) {
              handler.handle (resources);
              resources.handleContentStreams (this);
            }
          }
        };
    traversePageNodes (resourceContainerHandler);
    traverseAnnotations (new Cinderella<PDFDictionary> () {
      public boolean isGood (PDFDictionary annotation) {
        PDFDictionary appearanceDictionary = (PDFDictionary) annotation.get ("AP");
        if (appearanceDictionary != null)
          for (Object object : appearanceDictionary) {
            if (object instanceof PDFStream)
              resourceContainerHandler.handle ((PDFStream) object);
            else
              for (Object appearanceStream : (PDFDictionary) object)
                resourceContainerHandler.handle ((PDFStream) appearanceStream);
          }
        return true;
      }
    });
  }
  
  public <C> void traverseResourceType (final String resourceType,final Handler<C> handler) {
	    traverseResources (new Handler<PDFDictionary> () {
	      public void handle (PDFDictionary resources) {
	        PDFDictionary typedResources = (PDFDictionary) resources.get (resourceType);
	        if (typedResources != null)
	          for (Object typedResource : typedResources)
	            handler.handle ((C) typedResource);
	      }
	    });
	  }

  public void traverseFonts (final Handler<PDFDictionary> handler) {
	  traverseResourceType ("Font",handler);
  }
  
  public void traverseExternalObjects (final Handler<PDFStream> handler) {
	  traverseResourceType ("XObject",handler);
  }
  
  public void traverseExternalImages (final Handler<PDFStream> handler) {
    traverseExternalObjects (new Handler<PDFStream> () {
      public void handle (PDFStream xobject) {
        if (xobject.isOfSubtype ("Image"))
          handler.handle (xobject);
      }
    });
  }

  public void setCreator (String creator) {
    setInfoString ("Creator",creator);
    getRDFGraph ().addAttribute ("xap:CreatorTool",creator,"http://ns.adobe.com/xap/1.0/");
  }

  public void setProducer (String producer) {
    setInfoString ("Producer",producer);
    getRDFGraph ().addAttribute ("pdf:Producer",producer,"http://ns.adobe.com/pdf/1.3/");
  }

  public void setTitle (String title) {
    setInfoString ("Title",title);
    getRDFGraph ().addAttribute ("dc:title",title,"Alt","xml:lang","x-default","http://purl.org/dc/elements/1.1/");
  }

  public void setAuthor (String author) {
    setInfoString ("Author",author);
    getRDFGraph ().addAttribute ("dc:creator",author,"Seq",null,null,"http://purl.org/dc/elements/1.1/");
  }

  private RDFGraph getRDFGraph () { 
    PDFStream metadata = (PDFStream) root.get ("Metadata");
    if (metadata == null)
      return null;
    Document document;
    try {
      document = XMLUtil.getDocument (metadata.getInputStream ());
    } catch (IOException ioe) {
      ioe.printStackTrace();
      throw new Error ("couldn't read document metadata");
    }
    metadata.setStreamable (XMLUtil.getStreamable (document));
    Element documentElement = document.getDocumentElement ();
    Assertions.expect (documentElement.getNodeName (),"x:xmpmeta");
    NodeList graphs = documentElement.getElementsByTagName ("rdf:RDF");
    Assertions.expect (graphs.getLength (),1);
    return new RDFGraph (document,(Element) graphs.item (0));
  }

  void interpolate (PDFDocument document,String filename) throws IOException {
    final Set<Pair<PDFContainer,PDFContainer>> differences = new HashSet<Pair<PDFContainer,PDFContainer>> ();

    new PairHandler () {
      Map<PDFContainer,PDFContainer> compared = new HashMap<PDFContainer,PDFContainer> ();
      
      public boolean handle (PDFObject objectA,PDFObject objectB) {
        if (objectA instanceof PDFContainer) {
          PDFContainer containerA = (PDFContainer) objectA;
          PDFContainer containerB = (PDFContainer) objectB;
          PDFContainer old = compared.put (containerA,containerB);
          if (old == null) {
            // true if keys, atomic values, or types differ -- internals of descendants are irrelevant
            if (containerA.size () == containerB.size () && containerA.iterateInParallel (containerB,new PairHandler () {
              public boolean handle (PDFObject objectA,PDFObject objectB) {
                return
                (objectA == null && objectB == null) ||
                (objectA != null && objectB != null &&
                 objectA.getClass () == objectB.getClass () &&
                 (objectA instanceof PDFContainer || objectA.equals (objectB)));
              }
            }))
              containerA.iterateInParallel (containerB,this);
            else
              differences.add (new Pair<PDFContainer,PDFContainer> (containerA,containerB));
          }
          else if (old != objectB)
            throw new NotImplementedException ("structural difference interpolation");
        }
        return true;
      }
    }.handle (getRoot (),document.getRoot ());
    
    interpolate (document,differences.iterator (),filename);
  }

  private void interpolate (PDFDocument document,Iterator<Pair<PDFContainer, PDFContainer>> differences,String filename) throws IOException {
    if (differences.hasNext ()) {
      int n = 0;
      Pair<PDFContainer, PDFContainer> difference = differences.next ();
      while (difference.first.assimilate (difference.second))
        interpolate (document,differences,filename + "-" + ++n);
    }
    else
      new PDFWriter (filename + ".pdf").write (this);
  }
}
