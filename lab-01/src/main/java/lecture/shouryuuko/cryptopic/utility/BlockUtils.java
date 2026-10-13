package lecture.shouryuuko.cryptopic.utility;

import lecture.shouryuuko.cryptopic.algorithms.BlockCipher;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

public class BlockUtils {
    public static BlockCipher.Secret generateSecretECB(int n) throws NoSuchAlgorithmException {
        return new BlockCipher.Secret(generateKey(n), null);
    }

    public static BlockCipher.Secret generateSecretCBC(int n) throws NoSuchAlgorithmException {
        return new BlockCipher.Secret(generateKey(n), generateIV());
    }

    public static BlockCipher.Secret similarSecret(BlockCipher.Secret secret, int bits) {
        return new BlockCipher.Secret(BlockUtils.similarKey(secret.key(), bits), secret.arg());
    }

    public static SecretKey similarKey(SecretKey key, int bits) {
        var bytes = key.getEncoded();
        for (int i = 0; i < bits; ++i) {
            var block = i / 8;
            var item = i % 8;

            if (block < bytes.length) {
                bytes[block] = (byte) (bytes[block] ^ (1 << item));
            } else {
                break;
            }
        }

        return new SecretKeySpec(bytes, key.getAlgorithm());
    }

    public static SecretKey parseKey(String key) {
        return new SecretKeySpec(HexFormat.of().parseHex(key), "AES");
    }

    public static IvParameterSpec parseIv(String iv) {
        return new IvParameterSpec(HexFormat.of().parseHex(iv));
    }

    public static SecretKey generateKey(int n) throws NoSuchAlgorithmException {
        var generator = KeyGenerator.getInstance("AES");
        generator.init(n);

        return generator.generateKey();
    }

    public static IvParameterSpec generateIV() {
        var iv = new byte[16];
        new SecureRandom().nextBytes(iv);

        return new IvParameterSpec(iv);
    }
}
