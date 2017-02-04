/* 
 * bslocate is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * bslocate is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with bslocate. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package bslocate;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class BsLocate {

    static class Result {
        String line;
        long offset;
        public Result(long offset, String line) {
            this.line = line;
            this.offset = offset;
        }
    }
    
    static RandomAccessFile raf;
    static Pattern p;
    static int KEY;
    private static boolean VERBOSE = false;
    
    static Result binSearch(long s, long e) throws IOException {
//        System.out.format("%d %d\n", s, e);
        if (s > e)
            return null;
        long piv = (s + e) / 2;
        Result res = s == e? extractCurrentLine(raf, piv): extractNextLine(raf, piv);
        if (res.line == null) return null;
        Matcher m = p.matcher(res.line);
        if (VERBOSE) System.out.println("Line:\n" + res.line);
        if (!m.find()) return null;
        if (m.groupCount() == 0)
            return null;
        if (VERBOSE) System.out.println("Key: " + m.group(1) + "\n\n");
        int key = Integer.parseInt(m.group(1));
        if (s == e)
            return key == KEY? res: null;
        if (key < KEY)
            return binSearch(res.offset, e);
        Result l = binSearch(s, piv);
        if (l == null && key == KEY)
            return res;
        return l;
    }
    
    static Result binSearch(String regexp, int key, String file) throws IOException {
        KEY = key;
        if (VERBOSE) System.out.println("Compiling regexp: " + regexp);
        p = Pattern.compile(regexp);
        raf = new RandomAccessFile(new File(file), "r");
        Result res = binSearch(0, raf.length());
        return res;
    }
    
    static Result extractCurrentLine(RandomAccessFile raf, long p) throws IOException {
        p--;
        while (p >= 0) {
            raf.seek(p);
            if (raf.read() == '\n')
                break;
            p--;
        }
        if (p < 0) raf.seek(0);
        return new Result(raf.getFilePointer(), raf.readLine());
    }
    
    static Result extractNextLine(RandomAccessFile raf, long p) throws IOException {
        raf.seek(p);
        raf.readLine();
        long s = raf.getFilePointer();
        String l = raf.readLine();
//        System.out.println(l);
        return new Result(s, l);
    }
    
    @SuppressWarnings("resource")
    static void usage() throws IOException {
        new Scanner(BsLocate.class.getResource("README.org").openStream())
            .useDelimiter("\n")
            .forEachRemaining(System.out::println);
    }
    
    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            usage();
            return;
        }
        if (args.length == 4 && !"-p".equals(args[0])) {
            usage();
            return;
        }
        int i = args.length == 4? 1: 0;
        VERBOSE = args.length == 4;
        Result res = binSearch(args[i], Integer.parseInt(args[i + 1]), args[i + 2]);
        if (res == null)
            System.exit(-1);
        System.out.format("%d\n", res.offset);
        System.out.format("%s\n", res.line);
    }

    static String createFile() throws IOException {
        List<String> lines = Arrays.asList(
                "asdf 12:30 Twinkle, twinkle, little star,",
                "asdf 12:34 How I wonder what you are.",
                "asdf 12:34 Up above the world so high,",
                "asdf 12:34 Like a diamond in the sky.",
                "af 12:35 When the blazing sun is gone,",
                "af 12:35 When he nothing shines upon,",
                "af 12:35 Then you show your little light,",
                "af 12:36 Twinkle, twinkle, all the night.",
                "af 12:37 Then the traveler in the dark,",
                "af 12:37 Thanks you for your tiny spark,",
                "af 12:37 He could not see which way to go,",
                "af 12:38 If you did not twinkle so."
        );
        Path p = Files.createTempFile("g", "p");
        Files.write(p, lines, StandardOpenOption.WRITE);
        return p.toString();
    }
    
    @Test
    public void test_extractCurrentLine() throws IOException {
        String file = createFile();
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        
        assertEquals("asdf 12:30 Twinkle, twinkle, little star,", extractCurrentLine(raf, 10).line);
        assertEquals("asdf 12:34 Up above the world so high,", extractCurrentLine(raf, 89).line);
        assertEquals("asdf 12:30 Twinkle, twinkle, little star,", extractCurrentLine(raf, 0).line);
        assertEquals("asdf 12:30 Twinkle, twinkle, little star,", extractCurrentLine(raf, 40).line);
        assertEquals("asdf 12:30 Twinkle, twinkle, little star,", extractCurrentLine(raf, 41).line);
        assertEquals("af 12:38 If you did not twinkle so.", extractCurrentLine(raf, 451).line);
        assertEquals(442, extractCurrentLine(raf, 451).offset);
        assertEquals("af 12:38 If you did not twinkle so.", extractCurrentLine(raf, 477).line);
        assertEquals(null, extractCurrentLine(raf, 478).line);
        assertEquals("asdf 12:30 Twinkle, twinkle, little star,", extractCurrentLine(raf, 41).line);
    }
    
    @Test
    public void test_extractNextLine() throws IOException {
        String file = createFile();
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        
        assertEquals("asdf 12:34 How I wonder what you are.", extractNextLine(raf, 10).line);
        assertEquals("asdf 12:34 Like a diamond in the sky.", extractNextLine(raf, 89).line);
        assertEquals("asdf 12:34 How I wonder what you are.", extractNextLine(raf, 0).line);
        assertEquals("asdf 12:34 How I wonder what you are.", extractNextLine(raf, 40).line);
        assertEquals("asdf 12:34 How I wonder what you are.", extractNextLine(raf, 41).line);
        assertEquals(null, extractNextLine(raf, 451).line);
        assertEquals(478, extractNextLine(raf, 451).offset);
        assertEquals(null, extractNextLine(raf, 477).line);        
        assertEquals("asdf 12:34 How I wonder what you are.", extractNextLine(raf, 41).line);
    }
    
    @Test
    public void test_binSearch() throws IOException {
        String file = createFile();
        String keyRegxp = "^[a-z]* \\d*:(\\d*)";
        
        Result r = binSearch(keyRegxp, 36, file);
        assertEquals(276, r.offset);
        assertEquals("af 12:36 Twinkle, twinkle, all the night.", r.line);
        
        r = binSearch(keyRegxp, 35, file);
        assertEquals(157, r.offset);
        assertEquals("af 12:35 When the blazing sun is gone,", r.line);
        
        r = binSearch(keyRegxp, 30, file);
        assertEquals(0, r.offset);
        assertEquals("asdf 12:30 Twinkle, twinkle, little star,", r.line);
        
        r = binSearch(keyRegxp, 34, file);
        assertEquals(42, r.offset);
        assertEquals("asdf 12:34 How I wonder what you are.", r.line);
        
        r = binSearch(keyRegxp, 37, file);
        assertEquals(318, r.offset);
        assertEquals("af 12:37 Then the traveler in the dark,", r.line);
        
        r = binSearch(keyRegxp, 38, file);
        assertEquals(442, r.offset);
        assertEquals("af 12:38 If you did not twinkle so.", r.line);
    }
    
}
