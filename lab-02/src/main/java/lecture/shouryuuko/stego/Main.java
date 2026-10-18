package lecture.shouryuuko.stego;

import com.google.gson.Gson;
import lecture.shouryuuko.stego.utils.Meta;

import java.io.File;
import java.io.FileReader;

public class Main {
    public static void main(String[] args) {
        try (var reader = new FileReader("results/meta.json")) {
            var gson = new Gson();
            var meta = gson.fromJson(reader, Meta.class);

            var source = new File(meta.source());
            if (!source.exists() || !source.canRead()) {
                throw new IllegalArgumentException(String
                        .format("Resource %s does not exist or is not available for reading.", meta.source()));
            }

            var tester = new StegoTester();
            switch (meta.method()) {
                case "plain"            -> tester.testPlain(meta.getMessage(), source);
                case "encrypted"        -> tester.testEncrypted(meta.getPassword(), meta.getMessage(), source);
                case "encrypted random" -> tester.testEncryptedRandom(meta.getPassword(), meta.getMessage(), source);
                default -> throw new IllegalArgumentException("Unsupported method.");
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
