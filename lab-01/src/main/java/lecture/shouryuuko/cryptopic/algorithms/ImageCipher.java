package lecture.shouryuuko.cryptopic.algorithms;

public interface ImageCipher<S> {
    void encrypt(S secret, byte[] src, byte[] dest) throws Exception;
    void decrypt(S secret, byte[] src, byte[] dest) throws Exception;
}
