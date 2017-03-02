package id.bslocate;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

public class Util {
    
    static String createFile() throws IOException {
        List<String> lines = Arrays.asList(
                "asdf 12:30 Twinkle, twinkle, little star,",
                "asdf 12:34 How I wonder what you are.",
                "asdf 12:34 Up above the world so high,",
                "asdf 12:34 Like a diamond in the sky.",
                "af 12:35 When the blazing sun is gone,",
                "af 12:35 When he nothing shines upon,",
                "aaaaaaaaaaaaaaaaaaaaaaaaa adsasd",
                "aaaaaaaaaaaaaaaaaaaaaaaaa adsasd",
                "aaaaaaaaaaaaaaaaaaaaaaaaa adsasd",
                "aaaaaaaaaaaaaaaaaaaaaaaaa adsasd",
                "aaaaaaaaaaaaaaaaaaaaaaaaa adsasd",
                "af 12:35 Then you show your little light,",
                "af 12:36 Twinkle, twinkle, all the night.",
                "af 12:37 Then the traveler in the dark,",
                "af 12:37 Thanks you for your tiny spark,",
                "af 12:37 He could not see which way to go,",
                "af 12:38 If you did not twinkle so."
        );
        Path p = Files.createTempFile("g", "p");
        Files.write(p, lines, Charset.defaultCharset(), StandardOpenOption.WRITE);
        return p.toString();
    }
    
}
