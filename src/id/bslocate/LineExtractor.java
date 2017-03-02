package id.bslocate;

import java.io.IOException;
import java.io.RandomAccessFile;

public class LineExtractor {

    static interface Mapper {
        String map(String s);
    }
    
    private Mapper m;
    private RandomAccessFile raf;
    
    public LineExtractor(RandomAccessFile raf, Mapper m) {
        this.m = m;
        this.raf = raf;
    }

    public Line extractCurrentLine(long p) throws IOException {
        p--;
        while (p >= 0) {
            raf.seek(p);
            if (raf.read() == '\n')
                break;
            p--;
        }
        if (p < 0) raf.seek(0);
        long o = raf.getFilePointer();
        String line = raf.readLine();
        String k = m.map(line);
        if (k == null) return null;
        return new Line(o, line, k);
    }
    
    public Line extractNextLine(long p) throws IOException {
        raf.seek(p);
        raf.readLine();
        String k = null;
        long prev = raf.getFilePointer();
        while (true) {
            String l = raf.readLine();
            if (l == null) return null;
            k = m.map(l);
            if (k == null) continue;
            return new Line(prev, l, k);
        }
    }
    
}
