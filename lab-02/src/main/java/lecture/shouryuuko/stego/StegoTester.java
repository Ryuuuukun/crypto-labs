package lecture.shouryuuko.stego;

import lecture.shouryuuko.stego.stego.EncryptedRandomStego;
import lecture.shouryuuko.stego.stego.EncryptedStego;
import lecture.shouryuuko.stego.stego.PlainStego;
import lecture.shouryuuko.stego.utils.ImageUtils;
import lecture.shouryuuko.stego.utils.Metrics;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class StegoTester {
    private String currentSource;
    private String currentMethod;

    private boolean showHistogram;

    public void testPlain(String message, File source) throws IOException {
        var method = new PlainStego();

        var original = ImageUtils.repackImage(ImageUtils.loadImage(source), BufferedImage.TYPE_3BYTE_BGR);
        var stego = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_3BYTE_BGR);

        var originalBytes = ImageUtils.getBytes(original);
        var stegoBytes = ImageUtils.getBytes(stego);
        System.arraycopy(originalBytes, 0, stegoBytes, 0, originalBytes.length);

        method.inject(message.getBytes(), stegoBytes);
        var extract = method.extract(stegoBytes);

        this.currentMethod = "plain";
        this.currentSource = source.getName();

        ImageUtils.saveImage(stego, "results/stego_" + this.currentMethod.replaceAll(" +", "_") + ".png", "png");

        this.showMetrics(original, stego, message.getBytes(), extract);
        this.showDifferenceMap(original, originalBytes, stegoBytes);
        if (this.showHistogram) this.showHistogram(original, stego);
    }

    public void testEncrypted(String password, String message, File source) throws Exception {
        var method = new EncryptedStego();

        var original = ImageUtils.repackImage(ImageUtils.loadImage(source), BufferedImage.TYPE_3BYTE_BGR);
        var stego = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_3BYTE_BGR);

        var originalBytes = ImageUtils.getBytes(original);
        var stegoBytes = ImageUtils.getBytes(stego);
        System.arraycopy(originalBytes, 0, stegoBytes, 0, originalBytes.length);

        method.inject(password.getBytes(), message.getBytes(), stegoBytes);
        var extract = method.extract(password.getBytes(), stegoBytes);


        this.currentMethod = "encrypted";
        this.currentSource = source.getName();

        ImageUtils.saveImage(stego, "results/stego_" + this.currentMethod.replaceAll(" +", "_") + ".png", "png");

        this.showMetrics(original, stego, message.getBytes(), extract);
        this.showDifferenceMap(original, originalBytes, stegoBytes);
        if (this.showHistogram) this.showHistogram(original, stego);
    }

    public void testEncryptedRandom(String password, String message, File source) throws Exception {
        var method = new EncryptedRandomStego();

        var original = ImageUtils.repackImage(ImageUtils.loadImage(source), BufferedImage.TYPE_3BYTE_BGR);
        var stego = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_3BYTE_BGR);

        var originalBytes = ImageUtils.getBytes(original);
        var stegoBytes = ImageUtils.getBytes(stego);
        System.arraycopy(originalBytes, 0, stegoBytes, 0, originalBytes.length);

        method.inject(password.getBytes(), message.getBytes(), stegoBytes);
        var extract = method.extract(password.getBytes(), stegoBytes);


        this.currentMethod = "encrypted random";
        this.currentSource = source.getName();

        ImageUtils.saveImage(stego, "results/stego_" + this.currentMethod.replaceAll(" +", "_") + ".png", "png");

        this.showMetrics(original, stego, message.getBytes(), extract);
        this.showDifferenceMap(original, originalBytes, stegoBytes);
        if (this.showHistogram) this.showHistogram(original, stego);
    }

    protected void showDifferenceMap(BufferedImage image, byte[] original, byte[] stego) throws IOException {
        var mapImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        var mapBytes = ImageUtils.getBytes(mapImage);

        var difference = Metrics.differenceMap(original, stego);
        System.arraycopy(difference, 0, mapBytes, 0, difference.length);

        ImageUtils.saveImage(mapImage, "results/diffmap_" + this.currentMethod.replaceAll(" +", "_") + ".png", "png");
    }

    protected void showMetrics(BufferedImage original, BufferedImage stego, byte[] data, byte[] extract) {
        var originalBytes = ImageUtils.getBytes(original);
        var stegoBytes = ImageUtils.getBytes(stego);

        var psnr  = Metrics.psnr(original, stego);
        var ssim  = Metrics.ssim(originalBytes, stegoBytes);

        var xi2original = Metrics.xi2test(original);
        var xi2stego = Metrics.xi2test(stego);

        var ratio = Metrics.changedRatio(originalBytes, stegoBytes);

        var distOriginal = Metrics.distribution(originalBytes);
        var distStego = Metrics.distribution(stegoBytes);

        System.out.printf("""
                ┌───────────────────────────────────────────────────────────┐
                | Method: %-50s|
                | Source: %-50s|
                ├───────────────────────────────────────────────────────────┤
                | PSNR          | %41.5f |
                | SSIM          | %41.39f |
                | ratio         | %40.5f%% |
                | reversibility | %41b |
                ├───────────────────────────────────────────────────────────┤
                | Image |  P(LSB)=0  |  P(LSB)=1  |     xi²    |  p-value   |
                ├───────────────────────────────────────────────────────────┤
                | cover | %10.5f | %10.5f | %10.2f | %10.5f |
                | stego | %10.5f | %10.5f | %10.2f | %10.5f |
                └───────────────────────────────────────────────────────────┘
                """,
                this.currentMethod, this.currentSource,
                psnr, ssim, ratio,
                Arrays.compare(data, extract) == 0,
                distOriginal[0], distOriginal[1], xi2original[0], xi2original[1],
                distStego[0], distStego[1], xi2stego[0], xi2stego[1]
        );
    }

    protected void showHistogram(BufferedImage original, BufferedImage stego) {
        System.out.println("Histogarm for original image:");
        System.out.println(Arrays.toString(Metrics.histogram(original)));
        System.out.println("Histogram for stego image:");
        System.out.println(Arrays.toString(Metrics.histogram(stego)));
    }

    public boolean isShowHistogram() {
        return showHistogram;
    }

    public void setShowHistogram(boolean showHistogram) {
        this.showHistogram = showHistogram;
    }
}
