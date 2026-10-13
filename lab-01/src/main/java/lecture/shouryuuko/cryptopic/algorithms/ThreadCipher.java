package lecture.shouryuuko.cryptopic.algorithms;

public class ThreadCipher implements ImageCipher<Integer> {
    private int state;

    public void reset(int seed) {
        assert seed > 0;
        this.state = seed;
    }

    public int next() {
        this.state ^= this.state << 13;
        this.state ^= this.state >>> 17;
        this.state ^= this.state << 5;

        return this.state;
    }

    @Override
    public void encrypt(Integer secret, byte[] src, byte[] dest) {
        this.reset(secret);

        int state = 0;
        for (int i = 0; i < src.length; ++i) {
            if (i % 4 == 0) {
                state = this.next();
            }

            dest[i] = (byte) (src[i] ^ ((state >> ((i % 4) * 8)) & 0xFF));
        }
    }

    @Override
    public void decrypt(Integer secret, byte[] src, byte[] dest) {
        this.encrypt(secret, src, dest);
    }

    @Override
    public String toString() {
        return "xorshift";
    }
}
