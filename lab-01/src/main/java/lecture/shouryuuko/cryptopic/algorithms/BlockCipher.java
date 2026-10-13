package lecture.shouryuuko.cryptopic.algorithms;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.HexFormat;

public class BlockCipher implements ImageCipher<BlockCipher.Secret> {
    private final String algorithm;

    public BlockCipher(String algorithm) {
        this.algorithm = algorithm;
    }

    @Override
    public void encrypt(Secret secret, byte[] src, byte[] dest)
            throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {

        this.invoke(javax.crypto.Cipher.ENCRYPT_MODE, secret, src, dest);
    }

    @Override
    public void decrypt(Secret secret, byte[] src, byte[] dest)
            throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {

        this.invoke(javax.crypto.Cipher.DECRYPT_MODE, secret, src, dest);
    }

    protected void invoke(int method, Secret secret, byte[] src, byte[] dest)
            throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {

        var cipher = javax.crypto.Cipher.getInstance(this.algorithm);
        if (secret.arg() != null) {
            cipher.init(method, secret.key(), secret.arg());
        } else {
            cipher.init(method, secret.key());
        }


        var tmp = cipher.doFinal(src);
        System.arraycopy(tmp, 0, dest, 0, tmp.length);
    }

    @Override
    public String toString() {
        return this.algorithm.replaceAll("/", "-").toLowerCase();
    }

    public record Secret(
            SecretKey key,
            AlgorithmParameterSpec arg
    ) {
        public String getKeyHex() {
            return HexFormat.of().formatHex(key.getEncoded());
        }

        public String getArgHex() {
            if (arg == null) {
                return "null";
            }

            if (arg instanceof IvParameterSpec iv) {
                return HexFormat.of().formatHex(iv.getIV());
            } else {
                return "unsupported";
            }
        }

        @Override
        public String toString() {
            return String.format("[key=%s, arg=%s]", this.getKeyHex(), this.getArgHex());
        }
    }
}
