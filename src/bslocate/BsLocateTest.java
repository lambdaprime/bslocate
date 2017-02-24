package bslocate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import bslocate.BsLocate.Result;

import static bslocate.BsLocate.*;

public class BsLocateTest {

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
        Files.write(p, lines, Charset.defaultCharset(), StandardOpenOption.WRITE);
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
    
    static Result binSearch(String regexp, int key, String file) throws IOException {
        return BsLocate.binSearch(regexp, key, file, 0L, -1L);
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

    @Test
    public void test_binSearch_range() throws IOException {
        String file = createFile();
        String keyRegxp = "^[a-z]* \\d*:(\\d*)";
        
        Result r = BsLocate.binSearch(keyRegxp, 38, file, 318, -1);
        assertEquals(442, r.offset);
        assertEquals("af 12:38 If you did not twinkle so.", r.line);

        r = BsLocate.binSearch(keyRegxp, 38, file, 158, 318);
        assertTrue(r == null);

        r = BsLocate.binSearch(keyRegxp, 37, file, 158, 317);
        assertTrue(r == null);
    }

    @Test
    public void test_binSearch_upper_bound() throws IOException {
        String file = createFile();
        String keyRegxp = "^[a-z]* \\d*:(\\d*)";
        
        Result r = binSearch(keyRegxp, 31, file);
        assertEquals(42, r.offset);    
        assertEquals("asdf 12:34 How I wonder what you are.", r.line);
        
        r = binSearch(keyRegxp, 32, file);
        assertEquals(42, r.offset);    
        assertEquals("asdf 12:34 How I wonder what you are.", r.line);
        
        r = binSearch(keyRegxp, 33, file);
        assertEquals(42, r.offset);    
        assertEquals("asdf 12:34 How I wonder what you are.", r.line);
    }
    
}
