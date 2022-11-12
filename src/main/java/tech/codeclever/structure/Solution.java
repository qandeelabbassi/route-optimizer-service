package tech.codeclever.structure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class Solution {

    public final Data data;

    public int dist;

    public List<Route> routes = new ArrayList<>();

    public Solution(Data data) {
        this.data = data;
        this.dist = 0;
    }

    public Solution(Solution sol) {
        this.data = sol.data;
        this.dist = sol.dist;
        for (Route r : sol.routes) this.routes.add(new Route(r));
    }

    public void add(Route r) {
        this.dist += r.dist;
        this.routes.add(r);
    }

    public Route remove(int index) {
        this.dist -= this.routes.get(index).dist;
        return this.routes.remove(index);
    }

    public void emptyRouteCheck() {
        routes.removeIf(route -> route.tasks.size() == 2);
    }

    public boolean check_feasible() {
        if (data.max_vehicles > 0 && routes.size() > data.max_vehicles) {
            System.out.println("\tvehicles num " + routes.size() + " > " + data.max_vehicles);
            return false;
        }

        HashSet<Task> task_set = new HashSet<>(Arrays.asList(data.tasks.clone()));

        for (Route r : routes) {
            int r_load = 0;
            for (Task t : r.tasks) {
                // skip DEPOT
                if (t.equals(data.depot)) continue;
                r_load += t.demand;
                if (t.type == TaskType.EDGE) {
                    if (!task_set.contains(t) && !task_set.contains(data.get_reverse_edge(t))) {
                        System.out.println("\tlost task " + t);
                        return false;
                    }
                } else if (!task_set.contains(t)) {
                    System.out.println("\tlost task " + t);
                    return false;
                }
                task_set.remove(t);
                task_set.remove(data.get_reverse_edge(t));
            }
            if (r_load > data.max_capacity) {
                System.out.println("\tcapacity " + r_load + " > " + data.max_capacity);
                return false;
            }
            if (!(r.tasks.get(0).equals(data.depot)
                    && r.tasks.get(r.tasks.size() - 1).equals(data.depot))) {
                System.out.println("\tnot from depot to depot");
                return false;
            }
        }

        if (!task_set.isEmpty()) {
            System.out.println("\tstill some tasks not serve:");
            System.out.print("\t");
            for (Task t : task_set) {
                System.out.print("\t" + t);
            }
            System.out.println();
            return false;
        }
        return true;
    }

    public int getDist() {
        this.dist = 0;
        for (Route r : routes) this.dist += r.dist;
        return this.dist;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("Solution{cost=").append(dist).append(", solution=\n");
        for (Route r : routes) {
            sb.append(r.toString()).append("\n");
        }
        sb.append('}');

        return sb.toString();
    }
}
