package tech.codeclever;

import tech.codeclever.structure.*;

import java.util.ArrayList;
import java.util.List;

public class Initialization {

    public Solution articleWay(Data data) {
        return augmentMerge(data);
    }

    public Solution augmentMerge(Data data) {
        List<Route> route_pool = new ArrayList<>();
        // step 1 Initialize all tasks to form a single path
        for (Task t : data.tasks) {
            Route r = new Route(data);
            r.add(t);
            r.add(data.depot);
            route_pool.add(r);
        }
        // step 2 If one path is contained by another path, then merge the two paths
        boolean has_domain = true;
        while (has_domain) {
            has_domain = false;
            for (int i = 0; i < route_pool.size(); i++) {
                int j = 0;
                Route r = route_pool.get(i);
                while (j < route_pool.size()) {
                    if (i == j) {
                        j++;
                        continue;
                    }
                    if (r.merge(route_pool.get(j))) {
                        has_domain = true;
                        route_pool.remove(j);
                        break;
                    } else j++;
                }
            }
        }
        // step 3,4 Combine the two paths while meeting the capacity constraints and calculate their cost savings, choosing the maximum value
        boolean has_merge = true;
        while (has_merge) {
            has_merge = false;
            int pre = -1;
            int successor = -1;
            int min_change = Integer.MAX_VALUE;
            for (int i = 0; i < route_pool.size(); i++) {
                Route r1 = route_pool.get(i);
                for (int j = 0; j < route_pool.size(); j++) {
                    if (i == j) continue;
                    Route r2 = route_pool.get(j);
                    if (r1.load + r2.load > data.max_capacity) continue;
                    int change = r1.if_connect(r2);
                    if (change < min_change) {
                        pre = i;
                        successor = j;
                        min_change = change;
                        has_merge = true;
                    }
                }
            }
            if (has_merge) {
                route_pool.get(pre).connect(route_pool.get(successor));
                route_pool.remove(successor);
            }
        }
        // done
        Solution s = new Solution(data);
        for (Route r : route_pool) s.add(r);
        return s;
    }
}
