/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.adobe;

import java.util.Map;
import java.util.HashMap;

import info.joriki.util.CloneableObject;
import info.joriki.util.NotImplementedException;

public class Encoding extends CloneableObject
{
  // For purposes of glyph selection, non-breaking spaces
  // are equivalent to spaces and soft hyphens to hyphens.
  // For purposes of translation to Unicode, we distinguish them.
  // There seems to be some controversy regarding the exact
  // meaning and visibility of soft hyphens in ISO Latin and
  // in Unicode, but since their visual appearance is exactly
  // specified both in PDF and in SVG, we don't need to worry
  // about this and can safely assign Unicode's soft hyphen
  // to anything that's intended as some sort of soft hyphen.
  private final static String space = "space";
  private final static String nonBreakingSpace = new String (space);
  private final static String hyphen = "hyphen";
  private final static String softHyphen = new String (hyphen);

  private final static String [] ascii = {space,"exclam","quotedbl","numbersign","dollar","percent","ampersand","quotesingle","parenleft","parenright","asterisk","plus","comma",hyphen,"period","slash","zero","one","two","three","four","five","six","seven","eight","nine","colon","semicolon","less","equal","greater","question","at","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z","bracketleft","backslash","bracketright","asciicircum","underscore","grave","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z","braceleft","bar","braceright","asciitilde"};
  // The first entry (0xa0) in fancy had been "Euro" since the dawn of time.
  // There is nothing in any spec about this (PDF, CFF or PS),
  // and neither the Adobe Reader nor ghostscript use it.
  private final static String [] fancy = {null,"exclamdown","cent","sterling","fraction","yen","florin","section","currency","quotesingle","quotedblleft","guillemotleft","guilsinglleft","guilsinglright","fi","fl",null,"endash","dagger","daggerdbl","periodcentered",null,"paragraph","bullet","quotesinglbase","quotedblbase","quotedblright","guillemotright","ellipsis","perthousand",null,"questiondown",null,"grave","acute","circumflex","tilde","macron","breve","dotaccent","dieresis",null,"ring","cedilla",null,"hungarumlaut","ogonek","caron","emdash",null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,"AE",null,"ordfeminine",null,null,null,null,"Lslash","Oslash","OE","ordmasculine",null,null,null,null,null,"ae",null,null,null,"dotlessi",null,null,"lslash","oslash","oe","germandbls"};

  private final static String [] latin = {"dotlessi","grave","acute","circumflex","tilde","macron","breve","dotaccent","dieresis",null,"ring","cedilla",null,"hungarumlaut","ogonek","caron",nonBreakingSpace,"exclamdown","cent","sterling","currency","yen","brokenbar","section","dieresis","copyright","ordfeminine","guillemotleft","logicalnot",softHyphen,"registered","macron","degree","plusminus","twosuperior","threesuperior","acute","mu","paragraph","periodcentered","cedilla","onesuperior","ordmasculine","guillemotright","onequarter","onehalf","threequarters","questiondown","Agrave","Aacute","Acircumflex","Atilde","Adieresis","Aring","AE","Ccedilla","Egrave","Eacute","Ecircumflex","Edieresis","Igrave","Iacute","Icircumflex","Idieresis","Eth","Ntilde","Ograve","Oacute","Ocircumflex","Otilde","Odieresis","multiply","Oslash","Ugrave","Uacute","Ucircumflex","Udieresis","Yacute","Thorn","germandbls","agrave","aacute","acircumflex","atilde","adieresis","aring","ae","ccedilla","egrave","eacute","ecircumflex","edieresis","igrave","iacute","icircumflex","idieresis","eth","ntilde","ograve","oacute","ocircumflex","otilde","odieresis","divide","oslash","ugrave","uacute","ucircumflex","udieresis","yacute","thorn","ydieresis"};

  private final static String [] ansi = {"Euro",null,"quotesinglbase","florin","quotedblbase","ellipsis","dagger","daggerdbl","circumflex","perthousand","Scaron","guilsinglleft","OE",null,"Zcaron",null,null,"quoteleft","quoteright","quotedblleft","quotedblright","bullet","endash","emdash","tilde","trademark","scaron","guilsinglright","oe",null,"zcaron","Ydieresis",nonBreakingSpace,"exclamdown","cent","sterling","currency","yen","brokenbar","section","dieresis","copyright","ordfeminine","guillemotleft","logicalnot",softHyphen,"registered","macron","degree","plusminus","twosuperior","threesuperior","acute","mu","paragraph","periodcentered","cedilla","onesuperior","ordmasculine","guillemotright","onequarter","onehalf","threequarters","questiondown","Agrave","Aacute","Acircumflex","Atilde","Adieresis","Aring","AE","Ccedilla","Egrave","Eacute","Ecircumflex","Edieresis","Igrave","Iacute","Icircumflex","Idieresis","Eth","Ntilde","Ograve","Oacute","Ocircumflex","Otilde","Odieresis","multiply","Oslash","Ugrave","Uacute","Ucircumflex","Udieresis","Yacute","Thorn","germandbls","agrave","aacute","acircumflex","atilde","adieresis","aring","ae","ccedilla","egrave","eacute","ecircumflex","edieresis","igrave","iacute","icircumflex","idieresis","eth","ntilde","ograve","oacute","ocircumflex","otilde","odieresis","divide","oslash","ugrave","uacute","ucircumflex","udieresis","yacute","thorn","ydieresis"};

  private final static String [] roman = {"Adieresis","Aring","Ccedilla","Eacute","Ntilde","Odieresis","Udieresis","aacute","agrave","acircumflex","adieresis","atilde","aring","ccedilla","eacute","egrave","ecircumflex","edieresis","iacute","igrave","icircumflex","idieresis","ntilde","oacute","ograve","ocircumflex","odieresis","otilde","uacute","ugrave","ucircumflex","udieresis","dagger","degree","cent","sterling","section","bullet","paragraph","germandbls","registered","copyright","trademark","acute","dieresis","notequal","AE","Oslash","infinity","plusminus","lessequal","greaterequal","yen","mu","partialdiff","summation","product","pi","integral","ordfeminine","ordmasculine","Omega","ae","oslash","questiondown","exclamdown","logicalnot","radical","florin","approxequal","Delta","guillemotleft","guillemotright","ellipsis",nonBreakingSpace,"Agrave","Atilde","Otilde","OE","oe","endash","emdash","quotedblleft","quotedblright","quoteleft","quoteright","divide","lozenge","ydieresis","Ydieresis","fraction","Euro","guilsinglleft","guilsinglright","fi","fl","daggerdbl","periodcentered","quotesinglbase","quotedblbase","perthousand","Acircumflex","Ecircumflex","Aacute","Edieresis","Egrave","Iacute","Icircumflex","Idieresis","Igrave","Oacute","Ocircumflex","apple","Ograve","Uacute","Ucircumflex","Ugrave","dotlessi","circumflex","tilde","macron","breve","dotaccent","ring","cedilla","hungarumlaut","ogonek","caron"};
  private final static int [] nulls =
  {0255,0260,0262,0263,0266,0267,0270,0271,0272,0275,0303,0305,0306,0327,0360};

  // Symbol also has an /apple charstring that isn't in the standard encoding.
  private final static String [] symb1 = {"space","exclam","universal","numbersign","existential","percent","ampersand","suchthat","parenleft","parenright","asteriskmath","plus","comma","minus","period","slash","zero","one","two","three","four","five","six","seven","eight","nine","colon","semicolon","less","equal","greater","question","congruent","Alpha","Beta","Chi","Delta","Epsilon","Phi","Gamma","Eta","Iota","theta1","Kappa","Lambda","Mu","Nu","Omicron","Pi","Theta","Rho","Sigma","Tau","Upsilon","sigma1","Omega","Xi","Psi","Zeta","bracketleft","therefore","bracketright","perpendicular","underscore","radicalex","alpha","beta","chi","delta","epsilon","phi","gamma","eta","iota","phi1","kappa","lambda","mu","nu","omicron","pi","theta","rho","sigma","tau","upsilon","omega1","omega","xi","psi","zeta","braceleft","bar","braceright","similar"};
  private final static String [] symb2 = {"Euro","Upsilon1","minute","lessequal","fraction","infinity","florin","club","diamond","heart","spade","arrowboth","arrowleft","arrowup","arrowright","arrowdown","degree","plusminus","second","greaterequal","multiply","proportional","partialdiff","bullet","divide","notequal","equivalence","approxequal","ellipsis","arrowvertex","arrowhorizex","carriagereturn","aleph","Ifraktur","Rfraktur","weierstrass","circlemultiply","circleplus","emptyset","intersection","union","propersuperset","reflexsuperset","notsubset","propersubset","reflexsubset","element","notelement","angle","gradient","registerserif","copyrightserif","trademarkserif","product","radical","dotmath","logicalnot","logicaland","logicalor","arrowdblboth","arrowdblleft","arrowdblup","arrowdblright","arrowdbldown","lozenge","angleleft","registersans","copyrightsans","trademarksans","summation","parenlefttp","parenleftex","parenleftbt","bracketlefttp","bracketleftex","bracketleftbt","bracelefttp","braceleftmid","braceleftbt","braceex",null,"angleright","integral","integraltp","integralex","integralbt","parenrighttp","parenrightex","parenrightbt","bracketrighttp","bracketrightex","bracketrightbt","bracerighttp","bracerightmid","bracerightbt"};
  private final static String [] zapf1 = {"space","a1","a2","a202","a3","a4","a5","a119","a118","a117","a11","a12","a13","a14","a15","a16","a105","a17","a18","a19","a20","a21","a22","a23","a24","a25","a26","a27","a28","a6","a7","a8","a9","a10","a29","a30","a31","a32","a33","a34","a35","a36","a37","a38","a39","a40","a41","a42","a43","a44","a45","a46","a47","a48","a49","a50","a51","a52","a53","a54","a55","a56","a57","a58","a59","a60","a61","a62","a63","a64","a65","a66","a67","a68","a69","a70","a71","a72","a73","a74","a203","a75","a204","a76","a77","a78","a79","a81","a82","a83","a84","a97","a98","a99","a100"};
  private final static String [] zapf2 = {"a101","a102","a103","a104","a106","a107","a108","a112","a111","a110","a109","a120","a121","a122","a123","a124","a125","a126","a127","a128","a129","a130","a131","a132","a133","a134","a135","a136","a137","a138","a139","a140","a141","a142","a143","a144","a145","a146","a147","a148","a149","a150","a151","a152","a153","a154","a155","a156","a157","a158","a159","a160","a161","a163","a164","a196","a165","a192","a166","a167","a168","a169","a170","a171","a172","a173","a162","a174","a175","a176","a177","a178","a179","a193","a180","a199","a181","a200","a182",null,"a201","a183","a184","a197","a185","a194","a198","a186","a195","a187","a188","a189","a190","a191"};

  private final static String [] doc = {"bullet","dagger","daggerdbl","ellipsis","emdash","endash","florin","fraction","guilsinglleft","guilsinglright","minus","perthousand","quotedblbase","quotedblleft","quotedblright","quoteleft","quoteright","quotesinglbase","trademark","fi","fl","Lslash","OE","Scaron","Ydieresis","Zcaron","dotlessi","lslash","oe","scaron","zcaron",null,"Euro"};
  private final static String [] accents = {"breve","caron","circumflex","dotaccent","hungarumlaut","ogonek","ring","tilde"};

  public final static Encoding PDFDocEncoding = new Encoding ();
  public final static Encoding dingbatsEncoding = new Encoding (true);
  public final static Encoding standardEncoding = new Encoding ();
  public final static Encoding isoLatinEncoding = new Encoding ();
  public final static Encoding macRomanEncoding = new Encoding ();
  public final static Encoding winAnsiEncoding = new Encoding ();
  public final static Encoding symbolEncoding = new Encoding ();
  public final static Encoding macOSEncoding;

  static {
    for (int i = 0x20;i <= 0x7e;i++)
      standardEncoding.glyphs [i] = 
        isoLatinEncoding.glyphs [i] =
        macRomanEncoding.glyphs [i] =
        winAnsiEncoding.glyphs [i] = 
        PDFDocEncoding.glyphs [i] = ascii [i - 0x20];

    for (int i = 0xa0;i <= 0xfb;i++)
      standardEncoding.glyphs [i] = fancy [i - 0xa0];
    for (int i = 0x90;i < 0x100;i++)
      isoLatinEncoding.glyphs [i] = latin [i - 0x90];
    for (int i = 0x80;i < 0x100;i++)
      macRomanEncoding.glyphs [i] = roman [i - 0x80];
    for (int i = 0x80;i < 0x100;i++)
      winAnsiEncoding. glyphs [i] = ansi  [i - 0x80];

    // differences between Mac Roman and Mac OS, Section 5.5.5
    macOSEncoding = (Encoding) macRomanEncoding.clone ();
    for (int i = 0;i < nulls.length;i++)
      macRomanEncoding.glyphs [nulls [i]] = null;
    macRomanEncoding.glyphs [0333] = "currency";

    // footnote 3 on page 554 (570) of the PDF spec
    for (int i = 0x20;i < 0x100;i++)
      if (winAnsiEncoding.glyphs [i] == null)
        winAnsiEncoding.glyphs [i] = "bullet";

    for (int i = 0x20;i <= 0x7e;i++)
      symbolEncoding.glyphs [i] = symb1 [i - 0x20];
    for (int i = 0xa0;i <= 0xfe;i++)
      symbolEncoding.glyphs [i] = symb2 [i - 0xa0];

    for (int i = 0x20;i <= 0x7e;i++)
      dingbatsEncoding.glyphs [i] = zapf1 [i - 0x20];
    for (int i = 0xa1;i <= 0xfe;i++)
      dingbatsEncoding.glyphs [i] = zapf2 [i - 0xa1];

    for (int i = 0x18;i < 0x20;i++)
      PDFDocEncoding.glyphs [i] = accents [i - 0x18];
    for (int i = 0x80;i <= 0xa0;i++)
      PDFDocEncoding.glyphs [i] = doc [i - 0x80];
    for (int i = 0xa1;i < 0x100;i++)
      PDFDocEncoding.glyphs [i] = ansi [i - 0x80];

    /*
      single quotes are handled differently:
      Standard and IsoLatin map the ascii single quotes
      ' and ` to quoteright and quoteleft, respectively,
      whereas WinAnsi and MacRoman map them to
      quotesingle and grave, respectively, which is
      in line with Unicode. The ascii array above
      uses the latter mapping, so we need to change
      Standard and IsoLatin.
      Unicode used to be taken from IsoLatin; if so,
      this must happen *before* this change.
    */
    standardEncoding.glyphs ['\''] =
      isoLatinEncoding.glyphs ['\''] = "quoteright";
    standardEncoding.glyphs ['`'] =
      isoLatinEncoding.glyphs ['`'] = "quoteleft";
  }

  public String [] glyphs;
  // This flag determines whether the separate ZapfDingbats glyph list
  // is used to look up Unicodes for this encoding.
  final boolean isDingbats;

  public Encoding ()
  {
    this (false);
  }

  public Encoding (String name)
  {
    this (isDingbats (name));
  }

  public Encoding (boolean isDingbats)
  {
    this (new String [256],isDingbats);
  }

  public Encoding (String [] glyphs)
  {
    this (glyphs,false);
  }

  public Encoding (String [] glyphs,String name)
  {
    this (glyphs,isDingbats (name));
  }

  public Encoding (String [] glyphs,boolean isDingbats)
  {
    this.glyphs = glyphs;
    this.isDingbats = isDingbats;
  }

  public Object clone ()
  {
    Encoding clone = (Encoding) super.clone ();
    clone.glyphs = glyphs.clone ();
    clone.glyphToCodeMap = null;
    return clone;
  }

  private static boolean isDingbats (String name)
  {
    return name.indexOf ("ZapfDingbats") != -1;
  }

  public static Encoding getEncoding (String name)
  {
    if (name.equals ("WinAnsiEncoding"))
      return winAnsiEncoding;
    if (name.equals ("MacRomanEncoding"))
      return macRomanEncoding;
    // When implementing MacExpertEncoding, reconsider nonsymbolic TrueType encoding.
    // The spec is currently ambiguous on how to handle this. If MacExpertEncoding is
    // used directly as an encoding, none of the items in the spec's algorithm applies.
    // The Adobe Reader treats such a font as symbolic. If MacExpertEncoding is used
    // as the BaseEncoding, the Reader doesn't take undefined glyphs from the standard
    // encoding (see TrueType.get[Native]DefaultEncoding ()).
    throw new NotImplementedException ("named encoding " + name);
  }

  public int getUnicode (int b)
  {
    String glyph = glyphs [b];
    return
      glyph == null ? 0 :
      glyph == softHyphen ? 0255 :
      glyph == nonBreakingSpace ? 0240 :
      GlyphList.getUnicode (glyph,isDingbats);
  }

  Map glyphToCodeMap;
  
  public Map getGlyphToCodeMap ()
  {
    if (glyphToCodeMap == null)
      {
        glyphToCodeMap = new HashMap ();
        for (char i = 0;i < glyphs.length;i++)
          // don't overwrite space with non-breaking space
          if (glyphs [i] != null && !glyphToCodeMap.containsKey (glyphs [i]))
            glyphToCodeMap.put (glyphs [i],new Character (i));
      }
    return glyphToCodeMap;
  }
}

/* The following was for translating to Unicode, but then I
     found the Adobe glyph list, which takes care of that.

     private final static char [] symb2Codes = {'\u20ac','\u03d2','\u2032','\u2264','\u2044','\u221e','\u0192','\u2663','\u2666','\u2665','\u2660','\u2194','\u2190','\u2191','\u2192','\u2193','\u00b0','\u00b1','\u2033','\u2265','\u00d7','\u221d','\u2202','\u2219','\u00f7','\u2260','\u2261','\u2248','\u2026',0,0,'\u21b5','\u2135','\u2111','\u211c','\u2118','\u2297','\u2295','\u2205','\u2229','\u222a','\u2283','\u2287','\u2284','\u2282','\u2286','\u2208','\u2209','\u2220','\u2207',0,0,0,'\u220f','\u221a','\u22c5','\u00ac','\u2227','\u2228','\u21d4','\u21d0','\u21d1','\u21d2','\u21d3','\u25ca','\u2329',0,0,0,'\u2211',0,0,0,0,0,0,0,0,0,0,0,'\u232a','\u222b','\u2320',0,'\u2321'};
  private final static String [] greek = {
    "Alpha","Beta","Gamma","Delta","Epsilon","Zeta","Eta","Theta","Iota","Kappa","Lambda","Mu","Nu","Xi","Omicron","Pi","Rho",null,"Sigma","Tau","Upsilon","Phi","Chi","Psi","Omega"};

  private final static String [] unicodeGlyphs = {
    // override Latin soft hyphen
    "hyphen",
    // characters that appear in ansi and/or standard but not iso
    // (for quoteleft and quoteright see comment below)
    "quoteleft",
    "quoteright",
    "quotedblleft",
    "quotedblright",
    "guilsinglleft",
    "guilsinglright",
    "trademark",
    "dagger",
    "daggerdbl",
    "endash",
    "emdash",
    // various letters
    "dotlessi",
    "Lslash",
    "lslash",
    "OE",
    "oe",
    // greek variants
    "phi",
    "phi1",
    "sigma1",
    "theta1",
    "omega1",
    // mathematical symbols in symb1
    "universal",
    "existential",
    "suchthat",
    "asteriskmath",
    "minus",
    "congruent",
    "therefore",
    "perpendicular",
    "similar"
    // The following don't seem to have Unicodes:
    // things ending in ex,tp,mid,bt : extension, top, middle, bottom
    // (exception : integraltp, integralbt)
    // sans/serif versions of copyright, trademark and registered
  };

  private final static char [] unicodes = {
    // hyphen
    '\u002d',
    // characters that appear in ansi and/or standard but not iso
    // (for quoteleft and quoteright see comment below)
    '\u2018',
    '\u2019',
    '\u201c',
    '\u201d',
    '\u2039',
    '\u203a',
    '\u2122',
    '\u2020',
    '\u2021',
    '\u2013',
    '\u2014',
    // various letters
    '\u0131',
    '\u0141',
    '\u0142',
    '\u0152',
    '\u0153',
    // greek variants
    '\u03d5',
    '\u03c6',
    '\u03c2',
    '\u03d1',
    '\u03d6',
    // mathematical symbols in symb1
    '\u2200',
    '\u2203',
    '\u220b',
    '\u2217',
    '\u2212',
    '\u2245',
    '\u2234',
    '\u22a5',
    '\u223c'
  };

  private final static Dictionary unicodeMap = new Hashtable ();

    for (char c = 0x20;c < 0x100;c++)
      if (isoLatinEncoding.glyphs [c] != null && !(0x7f <= c && c <= 0xa0))
unicodeMap.put (isoLatinEncoding.glyphs [c],new Character (c));

    for (int i = 0;i < greek.length;i++)
      if (greek [i] != null)
{
  unicodeMap.put (greek [i],new Character ((char) (0x391 + i)));
  unicodeMap.put (greek [i].toLowerCase (),
  new Character ((char) (0x3b1 + i)));
}

    for (int i = 0;i < symb2Codes.length;i++)
      if (symb2Codes [i] != 0)
unicodeMap.put (symb2 [i],new Character (symb2Codes [i]));

    // overwrites phi with phi1
    for (int i = 0;i < unicodes.length;i++)
    unicodeMap.put (unicodeGlyphs [i],new Character (unicodes [i]));
  */
