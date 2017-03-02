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

package id.bslocate;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class BsLocate {

    @SuppressWarnings("resource")
    static void usage() throws IOException {
        Scanner scanner = new Scanner(BsLocate.class.getResource("README.org").openStream())
                .useDelimiter("\n");
        while (scanner.hasNext())
            System.out.println(scanner.next());
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
        boolean v= opts.contains("-v");
        long s = optionValue(opts, "-s");
        s = s == -1? 0: s;
        long e = optionValue(opts, "-e");        
        String regexp = args[args.length - 3];
        String key = args[args.length - 2];
        String file = args[args.length - 1];
        Line res = new BinSearch(regexp, Integer.parseInt(key), file, v).run(s, e);
        if (res == null)
            System.exit(-1);
        System.out.format("%d\n", res.offset);
        System.out.format("%s\n", res.line);
    }

    
}
