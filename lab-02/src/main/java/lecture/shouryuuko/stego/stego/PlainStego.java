package lecture.shouryuuko.stego.stego;

public class PlainStego {
    public void inject(byte[] data, byte[] container) {
        StegoUtils.linearInject(data, container);
    }

    public byte[] extract(byte[] container) {
        return StegoUtils.linearExtract(container);
    }
}
