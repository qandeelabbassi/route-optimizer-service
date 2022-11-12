package tech.codeclever.structure;

public class NodeTask extends Task {

    public NodeTask(String name, int demand, int cost, int node) {
        super(name, TaskType.NODE, demand, cost, node, node, 0);
    }

    @Override
    public String toString() {
        return name + "-Node{" + from + '}';
    }
}
