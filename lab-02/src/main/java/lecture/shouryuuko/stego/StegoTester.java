package lecture.shouryuuko.stego;

import lecture.shouryuuko.stego.stego.EncryptedRandomStego;
import lecture.shouryuuko.stego.stego.EncryptedStego;
import lecture.shouryuuko.stego.stego.PlainStego;
import lecture.shouryuuko.stego.utils.ImageUtils;
import lecture.shouryuuko.stego.utils.Metrics;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class StegoTester {
    private String currentSource;
    private String currentMethod;

    public void testPlain(String message, File source) throws IOException {
        var method = new PlainStego();

        var image = ImageUtils.repackImage(ImageUtils.loadImage(source), BufferedImage.TYPE_3BYTE_BGR);

        var stego = ImageUtils.getBytes(image);
        var original = Arrays.copyOf(stego, stego.length);

        method.inject(message.getBytes(), stego);
        var extract = method.extract(stego);

        this.currentMethod = "plain";
        this.currentSource = source.getName();
        this.showMetrics(original, stego, message.getBytes(), extract);
        this.showDifferenceMap(image, original, stego);
    }

    public void testEncrypted(String password, String message, File source) throws Exception {
        var method = new EncryptedStego();

        var image = ImageUtils.repackImage(ImageUtils.loadImage(source), BufferedImage.TYPE_3BYTE_BGR);

        var stego = ImageUtils.getBytes(image);
        var original = Arrays.copyOf(stego, stego.length);

        method.inject(password.getBytes(), message.getBytes(), stego);
        var extract = method.extract(password.getBytes(), stego);

        this.currentMethod = "encrypted";
        this.currentSource = source.getName();
        this.showMetrics(original, stego, message.getBytes(), extract);
        this.showDifferenceMap(image, original, stego);
    }

    public void testEncryptedRandom(String password, String message, File source) throws Exception {
        var method = new EncryptedRandomStego();

        var image = ImageUtils.repackImage(ImageUtils.loadImage(source), BufferedImage.TYPE_3BYTE_BGR);

        var stego = ImageUtils.getBytes(image);
        var original = Arrays.copyOf(stego, stego.length);

        method.inject(password.getBytes(), message.getBytes(), stego);
        var extract = method.extract(password.getBytes(), stego);

        this.currentMethod = "encrypted random";
        this.currentSource = source.getName();
        this.showMetrics(original, stego, message.getBytes(), extract);
        this.showDifferenceMap(image, original, stego);
    }

    protected void showDifferenceMap(BufferedImage image, byte[] original, byte[] stego) throws IOException {
        var mapImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        var mapBytes = ImageUtils.getBytes(mapImage);

        var difference = Metrics.differenceMap(original, stego);
        System.arraycopy(difference, 0, mapBytes, 0, difference.length);

        ImageUtils.saveImage(mapImage, "results/diffmap_" + this.currentMethod.replaceAll(" +", "_") + ".png", "png");
    }

    protected void showMetrics(byte[] original, byte[] stego, byte[] data, byte[] extract) {
        var pnsr  = Metrics.psnr(original, stego);
        var ssim  = Metrics.ssim(original, stego);
        var xi2   = Metrics.xi2test(original, stego);
        var ratio = Metrics.changedRatio(original, stego);

        var distOriginal = Metrics.distribution(original);
        var distStego = Metrics.distribution(stego);

        System.out.printf("""
                ┌─────────────────────────────────┐
                | Method: %-24s|
                | Source: %-24s|
                ├─────────────────────────────────┤
                | PNSR          | %15.5f |
                | SSIM          | %15.5f |
                | p-value       | %15.5f |
                | xi²           | %15.5f |
                | ratio         | %14.5f%% |
                | reversibility | %15b |
                ├─────────────────────────────────┤
                | Image |  P(LSB)=0  |  P(LSB)=1  |
                ├─────────────────────────────────┤
                | cover | %10.5f | %10.5f |
                | stego | %10.5f | %10.5f |
                └─────────────────────────────────┘
                """,
                this.currentMethod, this.currentSource,
                pnsr, ssim, xi2[1], xi2[0], ratio,
                Arrays.hashCode(data) == Arrays.hashCode(extract),
                distOriginal[0], distOriginal[1],
                distStego[0], distStego[1]
        );
    }
}
