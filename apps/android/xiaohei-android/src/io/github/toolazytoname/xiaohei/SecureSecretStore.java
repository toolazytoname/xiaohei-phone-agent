package io.github.toolazytoname.xiaohei;

import android.content.Context;
import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

/** Stores one channel token per non-exportable Android Keystore key. */
final class SecureSecretStore {
    enum Slot {
        PHONE_AGENT("phone_agent", "xiaohei.phone_agent.token.v1"),
        CONVERSATION("conversation", "xiaohei.conversation.token.v1");
        final String id;
        final String alias;
        Slot(String id, String alias) { this.id = id; this.alias = alias; }
    }

    // Compatibility wrappers retain existing Phone Agent callers.
    static void save(Context context, String value) throws Exception { save(context, Slot.PHONE_AGENT, value); }
    static boolean isConfigured(Context context) { return isConfigured(context, Slot.PHONE_AGENT); }
    static String load(Context context) throws Exception { return load(context, Slot.PHONE_AGENT); }
    static void clear(Context context) { clear(context, Slot.PHONE_AGENT); }

    static void save(Context context, Slot slot, String value) throws Exception {
        if (value == null || value.isEmpty()) return;
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key(slot));
        byte[] ciphertext = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
        prefs(context, slot).edit()
            .putString("token_iv", Base64.encodeToString(cipher.getIV(), Base64.NO_WRAP))
            .putString("token_ciphertext", Base64.encodeToString(ciphertext, Base64.NO_WRAP))
            .apply();
    }

    static boolean isConfigured(Context context, Slot slot) {
        return prefs(context, slot).contains("token_ciphertext");
    }

    static String load(Context context, Slot slot) throws Exception {
        android.content.SharedPreferences prefs = prefs(context, slot);
        String iv = prefs.getString("token_iv", null);
        String ciphertext = prefs.getString("token_ciphertext", null);
        if (iv == null || ciphertext == null) return "";
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key(slot), new GCMParameterSpec(128,
            Base64.decode(iv, Base64.NO_WRAP)));
        return new String(cipher.doFinal(Base64.decode(ciphertext, Base64.NO_WRAP)),
            StandardCharsets.UTF_8);
    }

    static void clear(Context context, Slot slot) {
        prefs(context, slot).edit().clear().apply();
    }

    private static android.content.SharedPreferences prefs(Context context, Slot slot) {
        // Keep the original Phone Agent preference file so an upgrade preserves
        // the existing encrypted blob and its historical Keystore alias.
        String name = slot == Slot.PHONE_AGENT ? "secure_channel" : "secure_channel_" + slot.id;
        return context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    private static SecretKey key(Slot slot) throws Exception {
        KeyStore store = KeyStore.getInstance("AndroidKeyStore");
        store.load(null);
        if (store.containsAlias(slot.alias)) return (SecretKey) store.getKey(slot.alias, null);
        KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        generator.init(new KeyGenParameterSpec.Builder(slot.alias,
            KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build());
        return generator.generateKey();
    }
}
