package id.bslocate;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.junit.Test;

public class LineExtractorTest {

    @Test
    public void test_extractCurrentLine() throws IOException {
        String file = Util.createFile();
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        
        LineExtractor le = new LineExtractor(raf, new LineExtractor.Mapper() {
            @Override
            public String map(String s) {
                return "";
            }
        });
        assertEquals("asdf 12:30 Twinkle, twinkle, little star,", le.extractCurrentLine(10).line);
        assertEquals("asdf 12:34 Up above the world so high,", le.extractCurrentLine(89).line);
        assertEquals("asdf 12:30 Twinkle, twinkle, little star,", le.extractCurrentLine(0).line);
        assertEquals("asdf 12:30 Twinkle, twinkle, little star,", le.extractCurrentLine(40).line);
        assertEquals("asdf 12:30 Twinkle, twinkle, little star,", le.extractCurrentLine(41).line);
        assertEquals("af 12:36 Twinkle, twinkle, all the night.", le.extractCurrentLine(451).line);
        assertEquals(441, le.extractCurrentLine(451).offset);
        assertEquals("af 12:38 If you did not twinkle so.", le.extractCurrentLine(617).line);
        assertEquals(null, le.extractCurrentLine(643).line);
        assertEquals("asdf 12:30 Twinkle, twinkle, little star,", le.extractCurrentLine(41).line);
    }
    
    @Test
    public void test_extractNextLine() throws IOException {
        String file = Util.createFile();
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        
        LineExtractor le = new LineExtractor(raf, new LineExtractor.Mapper() {
            @Override
            public String map(String s) {
                return "";
            }
        });
        
        assertEquals("asdf 12:34 How I wonder what you are.", le.extractNextLine(10).line);
        assertEquals("asdf 12:34 Like a diamond in the sky.", le.extractNextLine(89).line);
        assertEquals("asdf 12:34 How I wonder what you are.", le.extractNextLine(0).line);
        assertEquals("asdf 12:34 How I wonder what you are.", le.extractNextLine(40).line);
        assertEquals("asdf 12:34 How I wonder what you are.", le.extractNextLine(41).line);
        assertEquals(null, le.extractNextLine(617));
        assertEquals(null, le.extractNextLine(617));
        assertEquals(null, le.extractNextLine(642));        
        assertEquals("asdf 12:34 How I wonder what you are.", le.extractNextLine(41).line);
    }
    
    @Test
    public void test_extractNextLine_until_match() throws IOException {
        String file = Util.createFile();
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        
        LineExtractor le = new LineExtractor(raf, new LineExtractor.Mapper() {
            @Override
            public String map(String s) {
                return s.contains("12:35")? s: null;
            }
        });
        
        assertEquals("af 12:35 Then you show your little light,", le.extractNextLine(234).line);
    }
}
