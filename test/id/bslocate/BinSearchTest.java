package id.bslocate;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

public class BinSearchTest {

    static Line binSearch(String regexp, int key, String file) throws IOException {
        return new BinSearch(regexp, key, file).run(0L, -1L);
    }
    
    @Test
    public void test_binSearch() throws IOException {
        String file = Util.createFile();
        String keyRegxp = "^[a-z]* \\d*:(\\d*)";
        
        Line r = binSearch(keyRegxp, 36, file);
        assertEquals(441, r.offset);
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
        assertEquals(483, r.offset);
        assertEquals("af 12:37 Then the traveler in the dark,", r.line);
        
        r = binSearch(keyRegxp, 38, file);
        assertEquals(607, r.offset);    
        assertEquals("af 12:38 If you did not twinkle so.", r.line);

    }

    @Test
    public void test_binSearch_range() throws IOException {
        String file = Util.createFile();
        String keyRegxp = "^[a-z]* \\d*:(\\d*)";
        
        Line r = new BinSearch(keyRegxp, 38, file).run(318, -1);
        assertEquals(607, r.offset);
        assertEquals("af 12:38 If you did not twinkle so.", r.line);

        r = new BinSearch(keyRegxp, 38, file).run(158, 318);
        assertEquals("af 12:35 Then you show your little light,", r.line);

        r = new BinSearch(keyRegxp, 37, file).run(158, 317);
        assertEquals("af 12:35 Then you show your little light,", r.line);
        
        r = new BinSearch(keyRegxp, 35, file).run(234, 441);
        assertEquals("af 12:35 Then you show your little light,", r.line);
        
        r = new BinSearch(keyRegxp, 40, file).run(234, 440);
        assertEquals("af 12:35 Then you show your little light,", r.line);
    }

    @Test
    public void test_binSearch_upper_bound() throws IOException {
        String file = Util.createFile();
        String keyRegxp = "^[a-z]* \\d*:(\\d*)";
        
        Line r = binSearch(keyRegxp, 31, file);
        assertEquals(42, r.offset);
        assertEquals("asdf 12:34 How I wonder what you are.", r.line);
        
        r = binSearch(keyRegxp, 32, file);
        assertEquals(42, r.offset);    
        assertEquals("asdf 12:34 How I wonder what you are.", r.line);
        
        r = binSearch(keyRegxp, 33, file);
        assertEquals(42, r.offset);    
        assertEquals("asdf 12:34 How I wonder what you are.", r.line);
        
        r = binSearch(keyRegxp, 40, file);
        assertEquals(607, r.offset);    
        assertEquals("af 12:38 If you did not twinkle so.", r.line);
        
        r = binSearch(keyRegxp, 41, file);
        assertEquals(607, r.offset);    
        assertEquals("af 12:38 If you did not twinkle so.", r.line);

        r = binSearch(keyRegxp, 15, file);
        assertEquals(0, r.offset);    
        assertEquals("asdf 12:30 Twinkle, twinkle, little star,", r.line);
    
    }
    
}
