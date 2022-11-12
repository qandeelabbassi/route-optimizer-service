package tech.codeclever.structure;

import tech.codeclever.util.MyParameter;

//import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Data {

    public String name;
    public int max_vehicles;
    public int max_capacity;

    public int nodes;
    public int arcs;
    public int edges;

    public int nodes_r;
    public int arcs_r;
    public int edges_r;
    public int total_requests;

    public int depot_node;
    public Task depot;
    public Task[] tasks;
    public Map<Task, Task> edge_set;

    public int[][] raw_dist;
    public int[][] dist; // use Dijkstra algorithm

    // graph[i][j] means task t( from i to j ) else null
    public Task[][] graph;

    // minimum traversal cost between r and t, for related_destructor
    public int max_min_travel_r_t;
    // the demand of task t
    public int max_demand_t;

    public Segment[][] segments;

    public void preprocess() {
        dist = new int[nodes + 1][nodes + 1];
        segments = new Segment[nodes + 1][nodes + 1];
        for (int i = 0; i < nodes + 1; i++) {
            for (int j = 0; j < nodes + 1; j++)
                if (i != j) {
                    dist[i][j] = raw_dist[i][j];
                    if (raw_dist[i][j] < MyParameter.BIG_NUM) {
                        List<Integer> path = new ArrayList<>();
                        path.add(i);
                        path.add(j);
                        segments[i][j] = new Segment(path, this);
                    }
                }
        }

        // Dijkstra algorithm, o(n^2)
        for (int node = 1; node <= nodes; node++) {
            // for one row
            boolean[] visited = new boolean[nodes + 1];
            int[] row_dist = dist[node];
            for (int i = 1; i <= nodes; i++) {
                // find min dist node
                int min_d = Integer.MAX_VALUE;
                int min_n = -1;
                for (int j = 1; j <= nodes; j++) {
                    if (!visited[j] && min_d >= row_dist[j]) {
                        min_d = row_dist[j];
                        min_n = j;
                    }
                }
                // mark
                visited[min_n] = true;
                // update other node
                for (int j = 1; j <= nodes; j++) {
                    if (!visited[j] && row_dist[j] > row_dist[min_n] + raw_dist[min_n][j]) {
                        row_dist[j] = row_dist[min_n] + raw_dist[min_n][j];
                        segments[node][j] = segments[node][min_n].connect(segments[min_n][j]);
                    }
                }
            }
        }

        // other preprocess
        total_requests = nodes_r + arcs_r + edges_r;
        depot = new NodeTask("N" + depot_node, 0, 0, depot_node);
        max_min_travel_r_t = get_max_min_travel_r_t(); // o(n)
        max_demand_t = get_max_demand(); // o(n)
    }

    // for test
    public void show() {
        String s = name + "=>" + "\n\t" +
                "max v=" + max_vehicles + "\n\t" +
                "max c=" + max_capacity + "\n\t" +
                "depot=" + depot_node + "\n\t" +
                "all n=" + nodes + "\n\t" +
                "all e=" + edges + "\n\t" +
                "all a=" + arcs + "\n\t" +
                "req n=" + nodes_r + "\n\t" +
                "req e=" + edges_r + "\n\t" +
                "req a=" + arcs_r;
        System.out.println(s);
    }

    public Task get_reverse_edge(Task t) {
        return edge_set.getOrDefault(t, t);
    }

    private int get_max_min_travel_r_t() {
        int res = 0;
        for (Task t : tasks) {
            if (t.type == TaskType.EDGE) {
                for (Task r : tasks) {
                    if (res < dist[t.to][r.from]) res = dist[t.to][r.from];
                    if (res < dist[t.from][r.from]) res = dist[t.from][r.from];
                    if (r.type == TaskType.EDGE) {
                        if (res < dist[t.to][r.to]) res = dist[t.to][r.to];
                        if (res < dist[t.from][r.to]) res = dist[t.from][r.to];
                    }
                }
            } else {
                for (Task r : tasks) {
                    if (res < dist[t.to][r.from]) res = dist[t.to][r.from];
                    if (r.type == TaskType.EDGE) {
                        if (res < dist[t.to][r.to]) res = dist[t.to][r.to];
                    }
                }
            }
        }
        return res;
    }

    private int get_max_demand() {
        int res = 0;
        for (Task t : tasks) {
            if (res < t.demand) res = t.demand;
        }
        return res;
    }
}
