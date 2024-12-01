package bspentspy;

public class Triplet<X, Y, Z> {
    public final X first;
    public final Y second;
    public final Z third;
    public Triplet(X x, Y y, Z z) {
        this.first = x;
        this.second = y;
        this.third = z;
    }
}