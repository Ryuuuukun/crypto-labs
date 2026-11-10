package lecture.shouryuuko.stego.utils;

import org.apache.commons.math3.special.Gamma;
import org.apache.commons.statistics.inference.ChiSquareTest;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;

public class Metrics {
    public static final double RED_IMPACT_COEFF   = 0.299;
    public static final double GREEN_IMPACT_COEFF = 0.587;
    public static final double BLUE_IMPACT_COEFF  = 0.114;

    public static double psnr(BufferedImage original, BufferedImage stego) {
        assert original.getWidth() == stego.getWidth();
        assert original.getHeight() == stego.getHeight();

        var mse = 0.0;
        for (int i = 0; i < original.getHeight(); ++i) {
            for (int j = 0; j < original.getWidth(); ++j) {
                mse += Math.pow(getLightmass(original.getRGB(i, j)) - getLightmass(stego.getRGB(i, j)), 2);
            }
        }

        return mse == 0 ? Double.POSITIVE_INFINITY :
                10 * Math.log10(255.0 * 255.0 / (mse / (original.getHeight() * original.getWidth())));
    }

    public static double getLightmass(int pixel) {
        return RED_IMPACT_COEFF    * ((pixel >> 16) & 0xFF) +
                GREEN_IMPACT_COEFF * ((pixel >> 8) & 0xFF)  +
                BLUE_IMPACT_COEFF  * (pixel & 0xFF);
    }

    public static double ssim(byte[] original, byte[] stego) {
        if (original.length != stego.length) {
            throw new IllegalArgumentException("Original and stego must have the same size.");
        }

        var meanA = 0.0;
        var meanB = 0.0;
        for (int i = 0; i < original.length; ++i) {
            meanA += (original[i] & 0xFF);
            meanB += (stego[i] & 0xFF);
        }
        meanA /= original.length;
        meanB /= original.length;

        var varA = 0.0;
        var varB = 0.0;
        var covAB = 0.0;

        for (int i = 0; i < original.length; ++i) {
            var valA = original[i] & 0xFF;
            var valB = stego[i] & 0xFF;

            varA += Math.pow(valA - meanA, 2);
            varB += Math.pow(valB - meanB, 2);
            covAB += (valA - meanA) * (valB - meanB);
        }

        varA /= original.length;
        varB /= original.length;
        covAB /= original.length;

        var c1 = Math.pow(0.01 * 255, 2);
        var c2 = Math.pow(0.03 * 255, 2);

        var numerator = (2 * meanA * meanB + c1) * (2 * covAB + c2);
        var denominator = (Math.pow(meanA, 2) + Math.pow(meanB, 2) + c1) * (varA + varB + c2);

        return numerator / denominator;
    }

    public static double changedRatio(byte[] original, byte[] stego) {
        if (original.length != stego.length) {
            throw new IllegalArgumentException("Original and stego must have the same size.");
        }

        var diff = 0;
        for (int i = 0; i < original.length; ++i) {
            if (original[i] != stego[i]) {
                ++diff;
            }
        }

        return (double) diff / original.length * 100.0;
    }

    public static double[] distribution(byte[] source) {
        var dist = new double[2];
        for (byte b : source) {
            ++dist[b & 0x1];
        }
        dist[0] /= source.length;
        dist[1] /= source.length;

        return dist;
    }

    public static byte[] differenceMap(byte[] original, byte[] stego) {
        if (original.length != stego.length) {
            throw new IllegalArgumentException("Original and stego must have the same size.");
        }

        var result = new byte[original.length];
        for (int i = 0; i < original.length; ++i) {
            result[i] = (byte) Math.abs((original[i] & 0xFF - stego[i] & 0xFF) * 255);
        }

        return result;
    }

    public static double[] xi2test(BufferedImage image) {
        var histogram = histogram(image);

        var xi2square = 0.0;
        var freedom = 0;

        for (int i = 0; i < 128; ++i) {
            var a = histogram[i * 2];
            var b = histogram[i * 2 + 1];

            var total = a + b;
            if (total == 0) continue;

            var expected = total / 2.0;
            xi2square +=
                    Math.pow(a - expected, 2) / expected +
                    Math.pow(b - expected, 2) / expected;

            ++freedom;
        }

        if (freedom == 0) {
            throw new IllegalArgumentException("Image contains no pixels");
        }

        var pvalue = Gamma.regularizedGammaQ(
                freedom / 2.0,
                xi2square
        );

        return new double[] { xi2square, pvalue };
    }

    public static long[] histogram(BufferedImage image) {
        var histogram = new long[256];
        for (int y = 0; y < image.getHeight(); ++y) {
            for (int x = 0; x < image.getWidth(); ++x) {
                ++histogram[(int) Metrics.getLightmass(image.getRGB(x, y))];
            }
        }

        return histogram;
    }
}
