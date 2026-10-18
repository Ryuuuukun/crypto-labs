package lecture.shouryuuko.stego.utils;

import java.util.Random;

public record Meta(
        String method,
        String source,
        String message,
        String password,

        Long messageSeed,
        Boolean messageAuto,
        Integer messageLength,

        Long passwordSeed,
        Boolean passwordAuto,
        Integer passwordLength
) {
    public String getMessage() {
        var result = message;
        if (result == null || messageAuto) {
            var length = messageLength == null ? 50 : messageLength;
            var random = new Random();
            if (messageSeed != null) {
                random.setSeed(messageSeed);
            }

            result = random.ints(length, 'a', 'z' + 1)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();
        }

        return result;
    }

    public String getPassword() {
        var result = password;
        if (result == null || passwordAuto) {
            var length = passwordLength == null ? 50 : passwordLength;
            var random = new Random();
            if (passwordSeed != null) {
                random.setSeed(passwordSeed);
            }

            result = random.ints(length, 'a', 'z' + 1)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();
        }

        return result;
    }
}
