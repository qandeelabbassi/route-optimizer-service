package tech.codeclever.structure;

public class Tuple<A, B> {

    public final A first;
    public final B second;

    public Tuple(A f, B s) {
        this.first = f;
        this.second = s;
    }

    @Override
    public String toString() {
        return "Tuple(" + first + "," + second + ')';
    }
}
