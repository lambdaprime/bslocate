package id.bslocate;

class Line {
    String line;
    String key;
    long offset;
    public Line(long offset, String line, String k) {
        this.line = line;
        this.offset = offset;
        this.key = k;
    }
}