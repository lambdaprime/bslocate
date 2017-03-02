package id.bslocate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BinSearch {

    private boolean verbose;
    private RandomAccessFile raf;
    private Pattern p;
    private int key;
    private LineExtractor le;

    public BinSearch(String regexp, int key, String file) throws FileNotFoundException {
        this(regexp, key, file, false);
    }
    
    public BinSearch(String regexp, int key, String file, boolean v) throws FileNotFoundException {
        this.key = key;
        if (verbose) System.out.println("Compiling regexp: " + regexp);
        p = Pattern.compile(regexp);
        raf = new RandomAccessFile(new File(file), "r");
        le = new LineExtractor(raf, new LineExtractor.Mapper() {
            @Override
            public String map(String l) {
                if (l == null) return null;
                Matcher m = p.matcher(l);
                if (verbose) System.out.println("Line:\n" + l);
                if (!m.find()) return null;
                if (m.groupCount() == 0)
                    return null;
                if (verbose) System.out.println("Key: " + m.group(1) + "\n\n");
                return m.group(1);
            }
        });
    }

    public Line run(long s, long e) throws IOException {
        if (e == -1) e = raf.length();
        Line res = binSearch(s, e);
        //if (res != null && res.offset > e) return null;
        return res;
    }
    
    public Line binSearch(long s, long e) throws IOException {
        //      System.out.format("%d %d\n", s, e);
        if (s > e)
            return null;
        long piv = (s + e) / 2;
        Line line = le.extractCurrentLine(piv);
        if (line == null) line = le.extractNextLine(piv);
        if (line == null) return null;
        int k = Integer.parseInt(line.key);
        if (s == e)
            return k >= key? line: null;
        Line l = k < key? binSearch(piv + 1, e): binSearch(s, piv);
        if (l == null)
            return line;
        int kl = Integer.parseInt(l.key);
        if (kl == key) return l;
        if (k == key) return line;
        return l;
    }

}
