package org.ruiyun.JavaSwingServer;

import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;

import java.io.UnsupportedEncodingException;
import java.security.Security;

public class SM3Utils {
  private static final String ENCODING = "UTF-8";
  static {
    Security.addProvider(new BouncyCastleProvider());
  }

  public static String encrypt(String paramStr) throws UnsupportedEncodingException {
    String resultHexString = "";
    byte[] srcData = paramStr.getBytes(ENCODING);
    byte[] resultHash = hash(srcData);
    resultHexString = ByteUtils.toHexString(resultHash);
    return resultHexString;
  }

  private static byte[] hash(byte[] srcData) {
    SM3Digest digest = new SM3Digest();
    digest.update(srcData,0,srcData.length);
    byte[] hash = new byte[digest.getDigestSize()];
    digest.doFinal(hash,0);
    return hash;
  }

}
