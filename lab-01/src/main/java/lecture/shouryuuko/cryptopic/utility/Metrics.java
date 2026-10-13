package lecture.shouryuuko.cryptopic.utility;

import java.awt.image.BufferedImage;

public class Metrics {
    public static final double RED_IMPACT_COEFF   = 0.299;
    public static final double GREEN_IMPACT_COEFF = 0.587;
    public static final double BLUE_IMPACT_COEFF  = 0.114;

    public static double hcorr(BufferedImage image) {
        return Metrics.corr(image, 1, 0);
    }

    public static double vcorr(BufferedImage image) {
        return Metrics.corr(image, 0, 1);
    }

    public static double corr(BufferedImage image, int px, int py) {
        var meanX = 0.0;
        var meanY = 0.0;
        for (int y = 0; y < image.getHeight(); ++y) {
            for (int x = 0; x < image.getWidth() - 1; ++x) {
                meanX += getLightmass(image.getRGB(x, y));
                meanY += getLightmass(image.getRGB(x + 1, y));
            }
        }

        long counts = (long) image.getHeight() * (image.getWidth() - 1);
        meanX /= counts;
        meanY /= counts;

        var num = 0.0;
        var denX = 0.0;
        var denY = 0.0;
        for (int y = 0; y < image.getHeight() - py; ++y) {
            for (int x = 0; x < image.getWidth() - px; ++x) {
                var dx = getLightmass(image.getRGB(x, y)) - meanX;
                var dy = getLightmass(image.getRGB(x + px, y + py)) - meanY;

                num += dx * dy;
                denX += dx * dx;
                denY += dy * dy;
            }
        }

        return num / Math.sqrt(denX * denY);
    }

    public static double getLightmass(int pixel) {
        return RED_IMPACT_COEFF    * ((pixel >> 16) & 0xFF) +
                GREEN_IMPACT_COEFF * ((pixel >> 8) & 0xFF)  +
                BLUE_IMPACT_COEFF  * (pixel & 0xFF);
    }

    public static double entropy(BufferedImage image) {
        var entropy = 0.0;
        var counts = image.getWidth() * image.getHeight();
        var histogram = Metrics.histogram(image);
        for (int i = 0; i < 256; ++i) {
            if (histogram[i] > 0) {
                var p = (double) histogram[i] / counts;
                entropy -= p * Math.log(p) / Math.log(2.0);
            }
        }

        return entropy;
    }

    public static int[] histogram(BufferedImage image) {
        var histogram = new int[256];
        for (int y = 0; y < image.getHeight(); ++y) {
            for (int x = 0; x < image.getWidth(); ++x) {
                ++histogram[(int) Metrics.getLightmass(image.getRGB(x, y))];
            }
        }

        return histogram;
    }

    public static double npcr(BufferedImage cryptoA, BufferedImage cryptoB) {
        assert cryptoA.getHeight() == cryptoB.getHeight();
        assert cryptoA.getWidth() == cryptoB.getWidth();

        int diffs = 0;
        for (int y = 0; y < cryptoA.getHeight(); ++y) {
            for (int x = 0; x < cryptoA.getWidth(); ++x) {
                if (cryptoA.getRGB(x, y) != cryptoB.getRGB(x, y)) {
                    ++diffs;
                }
            }
        }

        return (double) diffs / (cryptoA.getHeight() * cryptoA.getWidth()) * 100.0;
    }

    public static double uaci(BufferedImage cryptoA, BufferedImage cryptoB) {
        assert cryptoA.getHeight() == cryptoB.getHeight();
        assert cryptoA.getWidth() == cryptoB.getWidth();

        double sum = 0.0;
        for (int y = 0; y < cryptoA.getHeight(); ++y) {
            for (int x = 0; x < cryptoA.getWidth(); ++x) {
                sum += Math.abs(Metrics.getLightmass(cryptoA.getRGB(x, y)) -
                        Metrics.getLightmass(cryptoB.getRGB(x, y))) / 255.0;
            }
        }

        return sum / (cryptoA.getHeight() * cryptoB.getWidth()) * 100.0;
    }
}
