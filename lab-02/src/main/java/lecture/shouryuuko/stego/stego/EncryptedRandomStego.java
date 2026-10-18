package lecture.shouryuuko.stego.stego;

import lecture.shouryuuko.stego.cipher.CipherUtils;
import lecture.shouryuuko.stego.cipher.Secret;

import java.nio.ByteBuffer;

public class EncryptedRandomStego {
    public void inject(byte[] password, byte[] data, byte[] container) throws Exception {
        var secret = Secret.from(password);
        var encrypted = CipherUtils.encrypt(secret, data);

        var buffer = ByteBuffer.allocate(Secret.SALT_LENGTH_BYTE + Secret.IV_LENGTH_BYTE + encrypted.length)
                .put(secret.salt()).put(secret.iv()).put(encrypted)
                .array();
        StegoUtils.randomInject(password, buffer, container);
    }

    public byte[] extract(byte[] password, byte[] container) throws Exception {
        var extract = StegoUtils.randomExtract(password, container);

        var encrypted = new byte[extract.length - Secret.SALT_LENGTH_BYTE - Secret.IV_LENGTH_BYTE];
        var salt = new byte[Secret.SALT_LENGTH_BYTE];
        var iv = new byte[Secret.IV_LENGTH_BYTE];

        System.arraycopy(extract, 0, salt, 0, Secret.SALT_LENGTH_BYTE);
        System.arraycopy(extract, Secret.SALT_LENGTH_BYTE, iv, 0, Secret.IV_LENGTH_BYTE);
        System.arraycopy(extract, Secret.SALT_LENGTH_BYTE + Secret.IV_LENGTH_BYTE, encrypted, 0, encrypted.length);

        var secret = Secret.from(password, salt, iv);

        return CipherUtils.decrypt(secret, encrypted);
    }
}
