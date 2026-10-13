package lecture.shouryuuko.cryptopic;

import com.google.gson.Gson;
import lecture.shouryuuko.cryptopic.algorithms.BlockCipher;
import lecture.shouryuuko.cryptopic.algorithms.ThreadCipher;
import lecture.shouryuuko.cryptopic.utility.BlockUtils;
import lecture.shouryuuko.cryptopic.utility.Meta;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.SecureRandom;

public class Main {

    public static void main(String[] args) {
        try {
            var metaPath = args.length > 0 ? args[0] : "results/meta.json";

            Meta meta = null;
            var gson = new Gson();
            try (var reader = new FileReader(metaPath)) {
                meta = gson.fromJson(reader, Meta.class);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }

            if (meta == null) {
                throw new RuntimeException("meta is null!");
            }

            var tester = new CipherStand(meta.pathEncrypted(), meta.pathDecrypted());
            switch (meta.algorithm()) {
                case "thread" -> {
                    var thread = new ThreadCipher();
                    var secret = new SecureRandom().nextInt();

                    tester.testMetrics(thread, secret, new File(meta.source()))
                            .print(System.out);
                }
                case "aes-ecb" -> {
                    var ecb = new BlockCipher("AES/ECB/NoPadding");
                    var secret = BlockUtils.generateSecretECB(256);

                    tester.testMetrics(ecb, secret, new File(meta.source()))
                            .print(System.out);
                    tester.testSensivity(ecb, secret, BlockUtils.similarSecret(secret, 1), new File(meta.source()))
                            .print(System.out);
                }
                case "aes-cbc" -> {
                    var cbc = new BlockCipher("AES/CBC/NoPadding");
                    var secret = new BlockCipher.Secret(
                            BlockUtils.generateKey(256),
                            meta.iv() != null ? BlockUtils.parseIv(meta.iv()) : BlockUtils.generateIV()
                    );

                    tester.testMetrics(cbc, secret, new File(meta.source()))
                            .print(System.out);
                    tester.testSensivity(cbc, secret, BlockUtils.similarSecret(secret, 1), new File(meta.source()))
                            .print(System.out);
                }
                default -> throw new RuntimeException("Unsupported algorithm!");
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}