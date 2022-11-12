package tech.codeclever.structure;

import java.util.Objects;

public abstract class Task {

    public String name;

    public TaskType type;
    public int demand;
    public int cost;
    public int from;
    public int to;
    public int dist;

    public Task(String name, TaskType type, int demand, int cost, int from, int to, int dist) {
        this.name = name;
        this.type = type;
        this.demand = demand;
        this.cost = cost;
        this.from = from;
        this.to = to;
        this.dist = dist;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return from == task.from &&
                to == task.to &&
                type == task.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, from, to);
    }
}
