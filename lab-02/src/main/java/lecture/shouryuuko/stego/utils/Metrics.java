package lecture.shouryuuko.stego.utils;

import org.apache.commons.statistics.inference.ChiSquareTest;

import java.util.Arrays;

public class Metrics {
    public static double psnr(byte[] original, byte[] stego) {
        if (original.length != stego.length) {
            throw new IllegalArgumentException("Original and stego must have the same size.");
        }

        var mse = 0.0;
        for (int i = 0; i < original.length; ++i) {
            mse += Math.pow(original[i] & 0xFF - stego[i] & 0xFF, 2);
        }

        return mse == 0 ? Double.POSITIVE_INFINITY : 10 * Math.log10(255.0 * 255.0 / (mse / original.length));
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

    public static double[] xi2test(byte[] original, byte[] stego) {
        var histogramOriginal = histogram(original);
        var histogramStego = histogram(stego);

        var expected = Arrays.stream(histogramOriginal).mapToDouble(c -> c + 0.5).toArray();
        var observed = Arrays.stream(histogramStego).map(c -> c + 1).toArray();

        var result = ChiSquareTest.withDefaults().test(expected, observed);

        return new double[] { result.getStatistic(), result.getPValue() };
    }

    public static long[] histogram(byte[] source) {
        var histogram = new long[256];
        for (var b : source) {
            ++histogram[b & 0xFF];
        }

        return histogram;
    }
}
