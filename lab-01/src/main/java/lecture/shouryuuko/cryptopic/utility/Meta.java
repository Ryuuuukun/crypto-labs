package lecture.shouryuuko.cryptopic.utility;

public record Meta(
        String algorithm,
        String source,
        String iv,
        String pathEncrypted,
        String pathDecrypted
) {

}
