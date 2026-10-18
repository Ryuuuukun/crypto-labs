package lecture.shouryuuko.stego.cipher;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class CipherUtils {
    public static byte[] encrypt(Secret secret, byte[] data)
            throws InvalidAlgorithmParameterException, NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        return invoke(Cipher.ENCRYPT_MODE, secret, data);
    }

    public static byte[] decrypt(Secret secret, byte[] data)
            throws InvalidAlgorithmParameterException, NoSuchPaddingException,
            IllegalBlockSizeException, NoSuchAlgorithmException,
            BadPaddingException, InvalidKeyException {

        return invoke(Cipher.DECRYPT_MODE, secret, data);
    }

    protected static byte[] invoke(int mode, Secret secret, byte[] data)
            throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {

        var cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(mode, secret.key(), new GCMParameterSpec(128, secret.iv()));

        return cipher.doFinal(data);
    }
}
