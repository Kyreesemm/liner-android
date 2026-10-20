package fun.tryliner.music;

import android.util.Base64;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Locale;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@CapacitorPlugin(name = "LinerSigner")
public class LinerSignerPlugin extends Plugin {
    // These constants intentionally match the desktop Rust signer's obfuscated key material.
    private static final byte[] API_SEED = hex("55c35caa3f3c326daa630f90395d6636");
    private static final byte[] API_KEY = hex("2499562b8020c1b7f67d7491e4c37f671a0e8af37f50af2e05d5a7b0be6e311140f342ad31f6cbeab5baddd08e56558cb4e51d82690c3fb23082a7f49bba9b70");
    private static final byte[] API_CHECKSUM = hex("e209025583eef007");
    private final SecureRandom random = new SecureRandom();

    @PluginMethod
    public void signRequest(PluginCall call) {
        try {
            String method = call.getString("method", "GET").trim().toUpperCase(Locale.ROOT);
            String path = call.getString("path", "/").split("\\?", 2)[0].trim();
            String body = call.getString("body", "");
            long timestamp = call.getLong("timestamp", System.currentTimeMillis() / 1000L);
            byte[] nonceBytes = new byte[8];
            random.nextBytes(nonceBytes);
            String nonce = toHex(nonceBytes);
            String bodyHash = toHex(MessageDigest.getInstance("SHA-256").digest(body.getBytes(StandardCharsets.UTF_8)));
            String canonical = method + "\n" + path + "\n" + timestamp + "\n" + nonce + "\n" + bodyHash;
            byte[] signature = hmac(deriveKey(), canonical.getBytes(StandardCharsets.UTF_8));
            JSObject result = new JSObject();
            result.put("signature", Base64.encodeToString(signature, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING));
            result.put("timestamp", timestamp);
            result.put("nonce", nonce);
            call.resolve(result);
        } catch (Exception error) {
            call.reject("Unable to sign API request", error);
        }
    }

    private static byte[] deriveKey() throws Exception {
        byte[] stream = new byte[64];
        int offset = 0;
        for (int i = 0; offset < stream.length; i++) {
            byte[] input = new byte[API_SEED.length + 1];
            System.arraycopy(API_SEED, 0, input, 0, API_SEED.length);
            input[input.length - 1] = (byte) i;
            byte[] block = MessageDigest.getInstance("SHA-256").digest(input);
            int size = Math.min(block.length, stream.length - offset);
            System.arraycopy(block, 0, stream, offset, size);
            offset += size;
        }
        byte[] key = new byte[API_KEY.length];
        for (int i = 0; i < key.length; i++) key[i] = (byte) (API_KEY[i] ^ stream[i]);
        byte[] check = MessageDigest.getInstance("SHA-256").digest(key);
        for (int i = 0; i < API_CHECKSUM.length; i++) {
            if (check[i] != API_CHECKSUM[i]) throw new IllegalStateException("Signer key check failed");
        }
        return key;
    }

    private static byte[] hmac(byte[] key, byte[] message) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(message);
    }

    private static byte[] hex(String value) {
        byte[] result = new byte[value.length() / 2];
        for (int i = 0; i < result.length; i++) result[i] = (byte) Integer.parseInt(value.substring(i * 2, i * 2 + 2), 16);
        return result;
    }

    private static String toHex(byte[] value) {
        StringBuilder out = new StringBuilder(value.length * 2);
        for (byte item : value) out.append(String.format(Locale.ROOT, "%02x", item & 0xff));
        return out.toString();
    }
}
