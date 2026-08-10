package io.github.toolazytoname.xiaohei;

import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/** Fixed-scope in-memory AES-GCM backup envelope. It never writes plaintext or ciphertext to disk. */
final class RootEncryptedBackup {
    private static final int MAX_BYTES=16*1024, KEY_BYTES=32, IV_BYTES=12;
    static final class Envelope { final byte[] iv,ciphertext; Envelope(byte[] iv,byte[] ciphertext){this.iv=copy(iv);this.ciphertext=copy(ciphertext);} }
    static Envelope seal(byte[] key, byte[] plaintext) throws Exception {
        validateKey(key); if(plaintext==null||plaintext.length>MAX_BYTES)throw new IllegalArgumentException("backup size");
        byte[] iv=new byte[IV_BYTES];new SecureRandom().nextBytes(iv);Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.ENCRYPT_MODE,new SecretKeySpec(key,"AES"),new GCMParameterSpec(128,iv));return new Envelope(iv,cipher.doFinal(plaintext));
    }
    static byte[] restore(byte[] key, Envelope envelope) throws Exception {
        validateKey(key);if(envelope==null||envelope.iv.length!=IV_BYTES||envelope.ciphertext.length<16||envelope.ciphertext.length>MAX_BYTES+16)throw new IllegalArgumentException("invalid envelope");Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.DECRYPT_MODE,new SecretKeySpec(key,"AES"),new GCMParameterSpec(128,envelope.iv));return cipher.doFinal(envelope.ciphertext);
    }
    private static void validateKey(byte[] key){if(key==null||key.length!=KEY_BYTES)throw new IllegalArgumentException("key length");}
    private static byte[] copy(byte[] value){return Arrays.copyOf(value,value.length);}
}
