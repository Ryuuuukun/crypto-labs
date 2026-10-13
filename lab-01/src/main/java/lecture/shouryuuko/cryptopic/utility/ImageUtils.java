package lecture.shouryuuko.cryptopic.utility;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ImageUtils {
    public static BufferedImage loadImage(File file) throws IOException {
        return ImageIO.read(file);
    }

    public static void saveImage(BufferedImage image, String path, String format) throws IOException {
        ImageIO.write(image, format, new File(path));
    }

    public static BufferedImage repackImage(BufferedImage image, int format) {
        var repack = new BufferedImage(image.getWidth(), image.getHeight(), format);
        var graphic = repack.createGraphics();
        graphic.drawImage(image, 0, 0, null);
        graphic.dispose();

        return repack;
    }

    public static byte[] getBytes(BufferedImage image) {
        return ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
    }

    public static byte[] getHash(BufferedImage image) throws NoSuchAlgorithmException {
        var digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(ImageUtils.getBytes(image));
    }
}
