package tech.codeclever.structure;

public class ArcTask extends Task {

    public ArcTask(String name, int demand, int cost, int headNode, int tailNode, int dist) {
        super(name, TaskType.ARC, demand, cost, headNode, tailNode, dist);
    }

    @Override
    public String toString() {
        return name + "-Arc{" + from + "," + to + '}';
    }
}
