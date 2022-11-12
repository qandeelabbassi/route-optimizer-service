package tech.codeclever.structure;

public class EdgeTask extends Task {

    public EdgeTask(String name, int demand, int cost, int firstNode, int secondNode, int dist) {
        super(name, TaskType.EDGE, demand, cost, firstNode, secondNode, dist);
    }

    @Override
    public String toString() {
        return name + "-Edge{" + from + "," + to + '}';
    }
}
