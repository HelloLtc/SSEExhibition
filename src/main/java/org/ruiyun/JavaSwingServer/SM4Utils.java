package org.ruiyun.JavaSwingServer;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.Security;


/**
 * sm4加密算法工具类
 * @explain sm4加密、解密与加密结果验证
 *          可逆算法
 */

public class SM4Utils {
  static {
    Security.addProvider(new BouncyCastleProvider());
  }
  private static final String ENCODING = "UTF-8";

  public static final String ALGORITHM_NAME = "SM4";
  // 加密算法/分组加密模式/分组填充方式
  // PKCS5Padding-以8个字节为一组进行分组加密
  // 定义分组加密模式使用：PKCS5Padding
  public static final String ALGORITHM_NAME_ECB_PADDING = "SM4/ECB/PKCS5Padding";
  // 128-32位16进制；256-64位16进制
  public static final int DEFAULT_KEY_SIZE = 128;
  private static Cipher generateEcbCipher(String algorithmName, int mode, byte[] key) throws Exception {
    Cipher cipher = Cipher.getInstance(algorithmName, BouncyCastleProvider.PROVIDER_NAME);
  //  Key sm4Key = new SecretKeySpec(key, ALGORITHM_NAME);
    Key sm4Key = new SecretKeySpec(key, ALGORITHM_NAME);
    cipher.init(mode, sm4Key);
    return cipher;
  }
  /**
   * sm4加密
   */

  public static String encryptEcb(String hexKey, String paramStr) throws Exception {
    String cipherText = "";
    // 16进制字符串-->byte[]
    byte[] keyData = ByteUtils.fromHexString(hexKey);
    // String-->byte[]
    byte[] srcData = paramStr.getBytes(ENCODING);
    // 加密后的数组
    byte[] cipherArray = encrypt_Ecb_Padding(keyData, srcData);
    // byte[]-->hexString
    cipherText = ByteUtils.toHexString(cipherArray);
    return cipherText;
  }
  /**
   * 加密模式之Ecb
   */
  public static byte[] encrypt_Ecb_Padding(byte[] key, byte[] data) throws Exception {
    Cipher cipher = generateEcbCipher(ALGORITHM_NAME_ECB_PADDING, Cipher.ENCRYPT_MODE, key);
    return cipher.doFinal(data);
  }

  /**
   * sm4解密
   */
  public static String decryptEcb(String hexKey, String cipherText) throws Exception {
    // 用于接收解密后的字符串
    String decryptStr = "";
    // hexString-->byte[]
    byte[] keyData = ByteUtils.fromHexString(hexKey);
    // hexString-->byte[]
    byte[] cipherData = ByteUtils.fromHexString(cipherText);
    // 解密
    byte[] srcData = decrypt_Ecb_Padding(keyData, cipherData);
    // byte[]-->String
    decryptStr = new String(srcData, ENCODING);
    return decryptStr;
  }
  /**
   * 解密
   */
  public static byte[] decrypt_Ecb_Padding(byte[] key, byte[] cipherText) throws Exception {
    Cipher cipher = generateEcbCipher(ALGORITHM_NAME_ECB_PADDING, Cipher.DECRYPT_MODE, key);
    return cipher.doFinal(cipherText);
  }

  public static byte[] generateKey(int keySize) throws Exception{
    KeyGenerator kg = KeyGenerator.getInstance(ALGORITHM_NAME,BouncyCastleProvider.PROVIDER_NAME);
    kg.init(keySize);
    return kg.generateKey().getEncoded();
  }
}
