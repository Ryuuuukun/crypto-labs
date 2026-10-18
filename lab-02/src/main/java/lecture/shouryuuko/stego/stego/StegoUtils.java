package lecture.shouryuuko.stego.stego;

import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StegoUtils {
    public static void linearInject(byte[] data, byte[] container) {
        var buffer = ByteBuffer.allocate(data.length + Integer.BYTES)
                .putInt(data.length).put(data)
                .array();
        if (buffer.length * 8 > container.length) {
            throw new IllegalArgumentException("There is not enough space for data embedding.");
        }

        for (int i = 0; i < buffer.length * 8; ++i) {
            var item = i % 8;
            var block = i / 8;
            container[i] = (byte) ((container[i] & 0xFE) | ((buffer[block] >> item) & 0x1));
        }
    }

    public static byte[] linearExtract(byte[] container) {
        if (container.length < Integer.SIZE) {
            throw new IllegalStateException("Data extraction error. The data is corrupted, the length could not be extracted.");
        }

        var lenbuf = new byte[Integer.BYTES];
        for (int i = 0; i < Integer.SIZE; ++i) {
            var item = i % 8;
            var block = i / 8;
            lenbuf[block] |= (byte) ((container[i] & 0x1) << item);
        }
        var length = ByteBuffer.wrap(lenbuf).getInt();

        if (container.length - Integer.SIZE < length * 8) {
            throw new IllegalStateException("Data extraction error. The data is corrupted, the data could not be extracted.");
        }

        var data = new byte[length];
        for (int i = 0; i < length * 8; ++i) {
            var item = i % 8;
            var block = i / 8;
            data[block] |= (byte) ((container[i + Integer.SIZE] & 0x1) << item);
        }

        return data;
    }

    public static void randomInject(byte[] seed, byte[] data, byte[] container) throws NoSuchAlgorithmException {
        var buffer = ByteBuffer.allocate(data.length + Integer.BYTES)
                .putInt(data.length).put(data)
                .array();
        if (buffer.length * 8 > container.length) {
            throw new IllegalArgumentException("There is not enough space for data embedding.");
        }

        var indices = IntStream.range(0, container.length)
                .boxed()
                .collect(Collectors.toCollection(ArrayList::new));

        var random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(seed);

        Collections.shuffle(indices, random);

        for (int i = 0; i < buffer.length * 8; ++i) {
            var item = i % 8;
            var block = i / 8;
            container[indices.get(i)] = (byte) ((container[indices.get(i)] & 0xFE) |
                    ((buffer[block] >> item) & 0x1));
        }
    }

    public static byte[] randomExtract(byte[] seed, byte[] container) throws NoSuchAlgorithmException {
        if (container.length < Integer.SIZE) {
            throw new IllegalStateException("Data extraction error. The data is corrupted, the length could not be extracted.");
        }

        var indices = IntStream.range(0, container.length)
                .boxed()
                .collect(Collectors.toCollection(ArrayList::new));

        var random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(seed);

        Collections.shuffle(indices, random);

        var lenbuf = new byte[Integer.BYTES];
        for (int i = 0; i < Integer.SIZE; ++i) {
            var item = i % 8;
            var block = i / 8;
            lenbuf[block] |= (byte) ((container[indices.get(i)] & 0x1) << item);
        }
        var length = ByteBuffer.wrap(lenbuf).getInt();

        if (container.length - Integer.SIZE < length * 8) {
            throw new IllegalStateException("Data extraction error. The data is corrupted, the data could not be extracted.");
        }

        var data = new byte[length];
        for (int i = 0; i < length * 8; ++i) {
            var item = i % 8;
            var block = i / 8;
            data[block] |= (byte) ((container[indices.get(i + Integer.SIZE)] & 0x1) << item);
        }

        return data;
    }
}
