package lecture.shouryuuko.stego.cipher;

import javax.crypto.KDF;
import javax.crypto.SecretKey;
import javax.crypto.spec.HKDFParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public record Secret(
        byte[] iv,
        byte[] salt,
        SecretKey key
) {
    public static final String DEFAULT_CONTEXT = "AesSecretKey";

    public static final int SALT_LENGTH_BYTE = 16;
    public static final int IV_LENGTH_BYTE = 12;

    public static Secret from(byte[] password)
            throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {

        var salt = new byte[SALT_LENGTH_BYTE];
        var iv = new byte[IV_LENGTH_BYTE];

        var random = new SecureRandom();
        random.nextBytes(salt);
        random.nextBytes(iv);

        return Secret.from(password, salt, iv);
    }

    public static Secret from(byte[] password, byte[] salt, byte[] iv)
            throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {

        var kdf = KDF.getInstance("HKDF-SHA256");
        var spec = HKDFParameterSpec.ofExtract()
                .addIKM(password)
                .addSalt(salt)
                .thenExpand(DEFAULT_CONTEXT.getBytes(), 32);

        return new Secret(iv, salt, kdf.deriveKey("AES", spec));
    }
}
