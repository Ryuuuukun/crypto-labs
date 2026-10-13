package lecture.shouryuuko.cryptopic;

import lecture.shouryuuko.cryptopic.algorithms.ImageCipher;
import lecture.shouryuuko.cryptopic.utility.ImageUtils;
import lecture.shouryuuko.cryptopic.utility.Metrics;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;

public class CipherStand {
    private String pathEncryptedFile;
    private String pathDecryptedFile;

    public CipherStand() {

    }

    public CipherStand(String pathEncryptedFile, String pathDecryptedFile) {
        this.pathEncryptedFile = pathEncryptedFile;
        this.pathDecryptedFile = pathDecryptedFile;
    }

    public <S> SensivityTestResult<S> testSensivity(ImageCipher<S> algorithm, S secretA, S secretB, File file) throws Exception {
        var planeImage = ImageUtils.repackImage(ImageUtils.loadImage(file), BufferedImage.TYPE_3BYTE_BGR);
        var planeBytes = ImageUtils.getBytes(planeImage);

        var encryptImageA = new BufferedImage(planeImage.getWidth(), planeImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        var encryptImageB = new BufferedImage(planeImage.getWidth(), planeImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);

        var encryptBytesA = ImageUtils.getBytes(encryptImageA);
        var encryptBytesB = ImageUtils.getBytes(encryptImageB);

        algorithm.encrypt(secretA, planeBytes, encryptBytesA);
        algorithm.encrypt(secretB, planeBytes, encryptBytesB);

        return new SensivityTestResult<>(
                algorithm.toString(), file.getName(),
                secretA, secretB,
                Metrics.npcr(encryptImageA, encryptImageB), Metrics.uaci(encryptImageA, encryptImageB));
    }

    public <S> CipherTestResult testMetrics(ImageCipher<S> algorithm, S secret, File file) throws Exception {
        var planeImage = ImageUtils.repackImage(ImageUtils.loadImage(file), BufferedImage.TYPE_3BYTE_BGR);
        var planeBytes = ImageUtils.getBytes(planeImage);

        var encryptImage = new BufferedImage(planeImage.getWidth(), planeImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        var decryptImage = new BufferedImage(planeImage.getWidth(), planeImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);

        var encryptBytes = ImageUtils.getBytes(encryptImage);
        var decryptBytes = ImageUtils.getBytes(decryptImage);

        long enryptedTime = System.currentTimeMillis();
        algorithm.encrypt(secret, planeBytes, encryptBytes);
        enryptedTime = System.currentTimeMillis() - enryptedTime;

        long decryptedTime = System.currentTimeMillis();
        algorithm.decrypt(secret, encryptBytes, decryptBytes);
        decryptedTime = System.currentTimeMillis() - decryptedTime;


        if (this.pathEncryptedFile != null) {
            ImageUtils.saveImage(encryptImage, this.pathEncryptedFile + "/" +
                    file.getName().replaceAll("\\.[^.]+$", "") + "_" + algorithm + "_encrypt.png", "png");
        }
        if (this.pathDecryptedFile != null) {
            ImageUtils.saveImage(decryptImage, this.pathDecryptedFile + "/" +
                    file.getName().replaceAll("\\.[^.]+$", "") + "_" + algorithm + "_decrypt.png", "png");
        }

        var hashPlane = ImageUtils.getHash(planeImage);
        var hashDecrypt = ImageUtils.getHash(decryptImage);
        var reversibility = Arrays.compare(hashPlane, hashDecrypt) == 0;

        return new CipherTestResult(
                file.getName(),
                algorithm.toString(),

                Metrics.entropy(planeImage),
                Metrics.entropy(encryptImage),

                Metrics.vcorr(planeImage),
                Metrics.vcorr(encryptImage),

                Metrics.hcorr(planeImage),
                Metrics.hcorr(encryptImage),

                Metrics.histogram(planeImage),
                Metrics.histogram(encryptImage),

                enryptedTime,
                decryptedTime,
                reversibility
        );
    }

    public void setPathEncryptedFile(String pathEncryptedFile) {
        this.pathEncryptedFile = pathEncryptedFile;
    }

    public void setPathDecryptedFile(String pathDecryptedFile) {
        this.pathDecryptedFile = pathDecryptedFile;
    }

    public String getPathEncryptedFile() {
        return pathEncryptedFile;
    }

    public String getPathDecryptedFile() {
        return pathDecryptedFile;
    }

    public record SensivityTestResult<S>(
            String algorithm,
            String filename,

            S secretA,
            S secretB,

            double npcr,
            double uaci
    ) {
        public void print(PrintStream stream) {
            stream.print(this);
        }

        @Override
        public String toString() {
            return String.format("""
                    ┌────────────────────────────────────────────────┐
                    | Image                | %-23s |
                    | Algorithm            | %-23s |
                    | NPCR                 | %10.5f%%             |
                    | UACI                 | %10.5f%%             |
                    └────────────────────────────────────────────────┘
                    
                    Secret A: %s
                    Secret B: %s
                    """,
                    algorithm, filename,
                    npcr, uaci,
                    secretA.toString(), secretB.toString()
            );
        }
    }

    public record CipherTestResult(
            String filename,
            String algorithm,

            double entropyPlane,
            double entropyEncrypt,

            double vcorrPlane,
            double vcorrEncrypt,

            double hcorrPlane,
            double hcorrEncrypt,

            int[] histogramPlane,
            int[] histogramEncrypt,

            long ecnryptedTime,
            long decryptedTime,
            boolean reversibility
    ) {
        public void print(PrintStream stream) {
            stream.print(this);
        }

        @Override
        public String toString() {
            return String.format("""
                    ┌────────────────────────────────────────────────┐
                    | Image                | %-23s |
                    | Algorithm            | %-23s |
                    | Encrypted time (ms)  | %-23d |
                    | Decrypted time (ms)  | %-23d |
                    | Reversibility        | %-23b |
                    ├────────────────────────────────────────────────┤
                    |  Image  |  Entropy   |   Corr V   |   Corr H   |
                    ├────────────────────────────────────────────────┤
                    | plane   | %10.5f | %10.5f | %10.5f |
                    | encrypt | %10.5f | %10.5f | %10.5f |
                    └────────────────────────────────────────────────┘
                    """,
                    filename, algorithm, ecnryptedTime, decryptedTime, reversibility,
                    entropyPlane, vcorrPlane, hcorrPlane,
                    entropyEncrypt, vcorrEncrypt, hcorrEncrypt
            );
        }
    }
}
