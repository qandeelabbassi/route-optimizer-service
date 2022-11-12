package tech.codeclever.structure;

import java.util.ArrayList;
import java.util.List;

public class Route {

    public final Data data;

    public int dist = 0;
    public int load = 0;

    public int nodes = 0;
    public int edges = 0;
    public int arcs = 0;

    public List<Task> tasks = new ArrayList<>();

    public Route(Data data) {
        this.data = data;
        nodes++;
        this.tasks.add(data.depot);
    }

    public Route(Route route) {
        this.data = route.data;
        this.dist = route.dist;
        this.load = route.load;
        this.nodes = route.nodes;
        this.edges = route.edges;
        this.arcs = route.arcs;
        this.tasks = new ArrayList<>(route.tasks);
    }

    public void add(Task t) {
        int last_node = tasks.get(tasks.size() - 1).to;
        tasks.add(t);
        load += t.demand;
        dist += (data.dist[last_node][t.from] + t.dist);
        switch (t.type) {
            case NODE:
                nodes++;
                break;
            case EDGE:
                edges++;
                break;
            case ARC:
                arcs++;
                break;
        }
    }

    public void add(Task t, int index) {

        // cannot add before depot or out of bounds
        if (index <= 0 || index >= tasks.size()) {
            System.out.println("add(index wrong) in route\n");
            System.exit(1);
        }

        // get pre node, and, next node
        int pre_node = tasks.get(index - 1).to;
        int nex_node = tasks.get(index).from;

        dist += (t.dist + data.dist[pre_node][t.from] + data.dist[t.to][nex_node] - data.dist[pre_node][nex_node]);
        load += t.demand;
        tasks.add(index, t);
        switch (t.type) {
            case NODE:
                nodes++;
                break;
            case EDGE:
                edges++;
                break;
            case ARC:
                arcs++;
                break;
        }
    }

    public void add(List<Task> task_list, int index) {
        while (!task_list.isEmpty()) {
            Task t = task_list.remove(0);
            add(t, index);
            index++;
        }
    }

    public Task remove(int index) {

        // cannot delete depot or out of bounds
        if (index <= 0 || index >= tasks.size() - 1) {
            System.out.println("remove(index wrong) in route");
            System.exit(1);
        }

        // get pre node, and, next node
        int pre_node = tasks.get(index - 1).to;
        int nex_node = tasks.get(index + 1).from;
        Task t = tasks.remove(index);
        dist += (data.dist[pre_node][nex_node] - t.dist - data.dist[pre_node][t.from] - data.dist[t.to][nex_node]);
        load -= t.demand;
        switch (t.type) {
            case NODE:
                nodes--;
                break;
            case EDGE:
                edges--;
                break;
            case ARC:
                arcs--;
                break;
        }
        return t;
    }

    public boolean greedyAdd(Task t) {
        if (t.demand + this.load > data.max_capacity) return false;
        int best_change = Integer.MAX_VALUE;
        int best_pos = -1;
        for (int insert = 1; insert < tasks.size(); insert++) {
            int pre = tasks.get(insert - 1).to;
            int next = tasks.get(insert).from;
            int cost = data.dist[pre][t.from] + t.dist + data.dist[t.to][next] - data.dist[pre][next];
            if (cost < best_change) {
                best_change = cost;
                best_pos = insert;
            }
        }
        if (best_pos == -1) return false;
        add(t, best_pos);
        return true;
    }

    public void reverse_all_edge_tasks() {
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            if (t.type != TaskType.EDGE) continue;
            remove(i);
            add(data.get_reverse_edge(t), i);
        }
    }

    public boolean merge(Route obj) {
        if (obj.tasks.size() <= 2) return true;
        if (obj.load + this.load > data.max_capacity) return false;
        List<Integer> add_index_list = new ArrayList<>();
        int check_index = 1;
        for (int i = 1; i < tasks.size() && check_index < obj.tasks.size() - 1; i++) {
            if (tasks.get(i - 1).to == tasks.get(i).from) continue;
            Segment s = data.segments[tasks.get(i - 1).to][tasks.get(i).from];
            Task t = obj.tasks.get(check_index);
            if (t.type == TaskType.NODE) {
                for (int j = 0; j < s.list.length; j++) {
                    if (s.list[j] == t.from) {
                        add_index_list.add(i);
                        check_index++;
                        break;
                    }
                }
            } else {
                for (int j = 1; j < s.list.length; j++) {
                    if (s.list[j - 1] == t.from && s.list[j] == t.to) {
                        add_index_list.add(i);
                        check_index++;
                        break;
                    }
                }
            }
        }
        if (add_index_list.size() == obj.tasks.size() - 2) {
            for (int i = 0; i < add_index_list.size(); i++) {
                add(obj.tasks.get(i + 1), add_index_list.get(i) + i);
            }
            return true;
        }
        return false;
    }

    public int if_connect(Route obj) {
        int last2 = tasks.get(tasks.size() - 2).to;
        int last1 = tasks.get(tasks.size() - 1).from;
        int first2 = obj.tasks.get(1).from;
        int first1 = obj.tasks.get(0).to;
        return data.dist[last2][first2] - data.dist[last2][last1] - data.dist[first1][first2];
    }

    public void connect(Route obj) {
        this.load += obj.load;
        this.nodes += (obj.nodes - 2);
        this.arcs += obj.arcs;
        this.edges += obj.edges;
        this.dist += (obj.dist + if_connect(obj));
        this.tasks.remove(tasks.size() - 1);
        this.tasks.addAll(obj.tasks.subList(1, obj.tasks.size()));
    }

    public Segment get_full_path() {
        Segment res = new Segment(new int[]{tasks.get(0).to}, data);
        for (int i = 1; i < tasks.size(); i++) {
            res = res.connect(data.segments[res.end][tasks.get(i).from]);
            res = res.connect(tasks.get(i));
        }
        return res;
    }

    @Override
    public String toString() {
        return "Route{" +
                "dist=" + dist +
                ", load=" + load +
                ", tasks=" + tasks +
                "}";
    }
}
