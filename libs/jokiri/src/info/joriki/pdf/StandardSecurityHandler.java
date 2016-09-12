/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import info.joriki.util.Assertions;
import info.joriki.util.NotImplementedException;

import info.joriki.crypto.MD5;
import info.joriki.crypto.ArcFourCypher;

public class StandardSecurityHandler implements SecurityHandler
{
  final static byte [] padding = {
    0x28,(byte) 0xBF,0x4E,0x5E,0x4E,0x75,(byte) 0x8A,0x41,
    0x64,0x00,0x4E,0x56,(byte) 0xFF,(byte) 0xFA,0x01,0x08,
    0x2E,0x2E,0x00,(byte) 0xB6,(byte) 0xD0,0x68,0x3E,(byte) 0x80,
    0x2F,0x0C,(byte) 0xA9,(byte) 0xFE,0x64,0x53,0x69,0x7A
  };
  
  MD5 digest = new MD5 ();
  
  byte [] first;
  byte [] owner;
  byte [] user;
  int permissions;
  int revision;
  int length;
  boolean encryptMetadata;

  StandardSecurityHandler (PDFDictionary encryptionDictionary,PDFArray id)
  {
    // patent documents from espacenet have no ID; passing empty array works
    first = id != null ? id.bytesAt (0) : new byte [0];

    owner = encryptionDictionary.getBytes ("O");
    user  = encryptionDictionary.getBytes ("U");
    permissions = encryptionDictionary.getInt ("P");
    revision = encryptionDictionary.getInt ("R");
    length = encryptionDictionary.getInt ("Length",40) >> 3;
    encryptMetadata = encryptionDictionary.getBoolean ("EncryptMetadata",true);
    
    if (encryptionDictionary.getInt ("V",0) >= 2)
      Assertions.expect (revision,3);
    if (user != null)
      Assertions.expect (user.length,padding.length);
    if (owner != null)
      Assertions.expect (owner.length,padding.length);
  }

  private byte [] computeEncryptionKey (String userPassword)
  {
    digest.reset ();
    digest.digest (padOrTruncate (userPassword));
    digest.digest (owner);
    digest.digest (permissions,4);
    digest.digest (first);
    if (!encryptMetadata) {
      digest.digest (-1,4);
      throw new NotImplementedException ("non-encrypted metadata");
    }

    // step 8 of algorithm 3.2 uses the length of the encryption key, whereas
    // step 3 of algorithm 3.3 uses the entire digest
    if (revision == 3 && length != 16)
      throw new NotImplementedException ();
    mashDigest ();
  
    return getEncryptionKey ();
  }
  
  private byte [] getEncryptionKey () {
    return padOrTruncate (digest.getDigest (),length);
  }
  
  private byte [] padOrTruncate (byte [] bytes,int n) {
    return padOrTruncate (bytes,new byte [n]);
  }
  
  private byte [] padOrTruncate (String password) {
    return padOrTruncate (password.getBytes (),padding);
  }
  
  private byte [] padOrTruncate (byte [] bytes,byte [] pad) {
    byte [] result = new byte [pad.length];
    int length = Math.min (bytes.length,result.length);
    System.arraycopy (bytes,0,result,0,length);
    int rest = result.length - length;
    if (rest > 0)
      System.arraycopy (pad,0,result,length,rest);
    return result;
  }
  
  private void mashDigest () {
    if (revision == 3)
      for (int i = 0;i < 50;i++) {
        byte [] output = digest.getDigest ();
        digest.reset ();
        digest.digest (output);
      }
  }

  private byte [] mashCypher (byte [] bytes,byte [] encryptionKey) {
    byte [] key = new byte [encryptionKey.length];
    for (int i = 0;i < (revision == 3 ? 20 : 1);i++) {
      for (int j = 0;j < key.length;j++)
        key [j] = (byte) (encryptionKey [j] ^ i);
      new ArcFourCypher (key).encrypt (bytes);
    }
    return bytes;
  }
  
  private byte [] generateUserPasswordPrefix (String userPassword) {
    byte [] encryptionKey = computeEncryptionKey (userPassword);
    byte [] bytes;
    switch (revision) {
    case 2 :
      bytes = padding.clone ();
      break;
    case 3:
      digest.reset ();
      digest.digest (padding);
      digest.digest (first);
      bytes = digest.getDigest ();
      break;
    default :
      throw new NotImplementedException ("standard security handler revision " + revision);
    }
    return mashCypher (bytes,encryptionKey);
  }
  
  private byte [] generateUserPasswordEntry (String userPassword) {
    return padOrTruncate (generateUserPasswordPrefix (userPassword),32);
  }

  public byte [] generateEncryptionKey () {
    return generateEncryptionKey ("");
  }
  
  private byte [] generateEncryptionKey (String userPassword) {
    return user.length == 32 && info.joriki.util.Arrays.isPrefix (generateUserPasswordPrefix (userPassword),user) ?
        computeEncryptionKey (userPassword) : null;
  }
  
  public void setPasswords (String userPassword,String ownerPassword) {
    owner = generateOwnerPasswordEntry (userPassword,ownerPassword);
    user = generateUserPasswordEntry (userPassword);
  }
  
  private byte [] generateOwnerPasswordEntry (String userPassword,String ownerPassword) {
    digest.reset ();
    digest.digest (padOrTruncate (ownerPassword));
    mashDigest ();
    return mashCypher (padOrTruncate (userPassword),getEncryptionKey ());
  }
  
  public boolean requestPermissions () {
    return java.util.Arrays.equals (generateOwnerPasswordEntry ("",""),owner);
  }
}
