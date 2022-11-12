package tech.codeclever.structure;

public class Deadhead extends Task {

    // only consider one direction
    // for edge, create two deadhead( from, to )( to, from )

    public Deadhead(String name, int from, int to, int dist) {
        super(name, TaskType.DEAD, 0, 0, from, to, dist);
    }

    @Override
    public String toString() {
        return name + "-Dead{" + from + "," + to + '}';
    }
}
