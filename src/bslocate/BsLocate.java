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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        if (l == null)
            return res;
        return l;
    }
    
    static Result binSearch(String regexp, int key, String file, long s, long e) throws IOException {
        KEY = key;
        if (VERBOSE) System.out.println("Compiling regexp: " + regexp);
        p = Pattern.compile(regexp);
        raf = new RandomAccessFile(new File(file), "r");
        if (e == -1) e = raf.length();
        Result res = binSearch(s, e);
        if (res != null && res.offset > e) return null;
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
    
    static long optionValue(List<String> opts, String opt) {
        if (!opts.contains(opt)) 
            return -1;
        int p = opts.indexOf(opt);
        if (p == -1) return -1;
        p++;
        if (p >= opts.size()) 
            throw new RuntimeException("Value for option " + opt + " is not set");
        return Long.parseLong(opts.get(p));
    }
    
    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            usage();
            return;
        }
        List<String> opts = Arrays.asList(args).subList(0, args.length - 3);
        VERBOSE = opts.contains("-v");
        long s = optionValue(opts, "-s");
        s = s == -1? 0: s;
        long e = optionValue(opts, "-e");        
        String regexp = args[args.length - 3];
        String key = args[args.length - 2];
        String file = args[args.length - 1];
        
        Result res = binSearch(regexp, Integer.parseInt(key), file, s, e);
        if (res == null)
            System.exit(-1);
        System.out.format("%d\n", res.offset);
        System.out.format("%s\n", res.line);
    }

    
}
