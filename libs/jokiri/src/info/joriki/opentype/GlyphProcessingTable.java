/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

import info.joriki.io.FullySeekableDataInput;
import info.joriki.sfnt.SFNTTable;
import info.joriki.util.Assertions;

import java.io.DataOutput;
import java.io.IOException;

abstract public class GlyphProcessingTable extends SFNTTable implements OffsetTable {
  final static int version = 0x00010000;
  
  public ScriptList scriptList = new ScriptList ();
  public FeatureList featureList = new FeatureList ();
  public LookupList lookupList = new LookupList (this);

  public GlyphProcessingTable (String id) {
    super (id);
  }
  
  public GlyphProcessingTable (String id,FullySeekableDataInput in) throws IOException {
    super (id);
    Assertions.expect (in.readInt (),version);
    in.pushOffset (in.readUnsignedShort ());
    scriptList.readFrom (in);
    in.popOffset ();
    in.pushOffset (in.readUnsignedShort ());
    featureList.readFrom (in);
    in.popOffset ();
    in.pushOffset (in.readUnsignedShort ());
    lookupList.readFrom (in);
    in.popOffset ();
  }
  
  public void writeTo(DataOutput out) throws IOException {
    OffsetOutputStream oos = new OffsetOutputStream ();
    oos.writeTable (this);
    out.write (oos.toByteArray ());
  }
  
  public void writeTo (OffsetOutputStream out) {
    out.writeShort (version >> 16);
    out.writeShort (version);
    out.writeOffset (scriptList);
    out.writeOffset (featureList);
    out.writeOffset (lookupList);
  }

  public void setupDefault () {
    ScriptList.ScriptTable defaultScriptTable = new ScriptList.ScriptTable ();
    defaultScriptTable.defaultLanguageSystemTable = new LanguageSystemTable ();
    scriptList.addTable ("DFLT",defaultScriptTable);
  }
  
  public void addFeature (String feature,int lookupType,LookupSubtable lookupSubtable) {
    ((ScriptList.ScriptTable) scriptList.getTable ("DFLT")).defaultLanguageSystemTable.add(featureList.tables.size ());
    FeatureList.FeatureTable featureTable = new FeatureList.FeatureTable ();
    featureTable.add (lookupList.tables.size ());
    featureList.addTable (feature,featureTable);
    LookupList.LookupTable lookupTable = lookupList.new LookupTable (lookupType,0);
    lookupTable.addTable(lookupSubtable);
    lookupList.addTable (lookupTable);
  }
  
  abstract protected LookupSubtable readSubtable (FullySeekableDataInput in,int type) throws IOException;
}
