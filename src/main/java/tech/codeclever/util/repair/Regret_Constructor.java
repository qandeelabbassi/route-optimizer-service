package tech.codeclever.util.repair;

import tech.codeclever.structure.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Regret_Constructor extends Constructor {

    //  Compute for each task t its cheapest insertion cost
    //  and its second cheapest insertion cost,
    //  and define its regret r(t) as
    //  the difference between the two costs.
    //  Insert the task having maximum regret
    //  in its best position and then reiterate,
    //  by recomputing regrets, until all tasks have been reinserted.

    private List<Integer> regret;
    private List<Integer> insert_route;
    private List<Integer> insert_index;
    private List<Task> tasks;

    @Override
    public void construct(List<Task> remains, Solution sol) {
        Data data = sol.data;
        Set<Task> set = list2set(remains, sol.data);
        while (set.size() > 0) {
            update_regret(set, sol, data);
            // find the max regret
            int index = 0;
            int max_regret = regret.get(0);
            for (int i = 1; i < regret.size(); i++) {
                if (max_regret < regret.get(i)) {
                    max_regret = regret.get(i);
                    index = i;
                }
            }
            if (insert_route.get(index) >= sol.routes.size()) {
                // add a new route
                Route r = new Route(data);
                r.add(data.depot);
                sol.add(r);
            }
            sol.routes.get(insert_route.get(index)).add(tasks.get(index), insert_index.get(index));
            set.remove(tasks.get(index));
            if (tasks.get(index).type == TaskType.EDGE) set.remove(data.get_reverse_edge(tasks.get(index)));
        }
    }

    private void update_regret(Set<Task> set, Solution s, Data data) {
        regret = new ArrayList<>(); // second_cost - first_cost
        insert_route = new ArrayList<>();
        insert_index = new ArrayList<>();
        tasks = new ArrayList<>();

        for (Task t : set) {
            boolean need_new_route = true;
            int best_route = -1;
            int best_index = -1;
            // a cost change we find is smaller than first_cost -> first
            // when first updated, first_cost -> second
            // a cost change we find is larger than first_cost but smaller than second_cost -> second
            int first_cost = Integer.MAX_VALUE - 1; // when get the better position, this will be updated
            int second_cost = Integer.MAX_VALUE; // when first updated, this will be updated

            for (int i = 0; i < s.routes.size(); i++) {
                Route r = s.routes.get(i);
                if (r.load + t.demand > data.max_capacity)
                    continue;
                // it means there is a route can add this task,
                // so "need_new_route" = false
                need_new_route = false;
                for (int j = 1; j < r.tasks.size(); j++) {

                    int pre_node = r.tasks.get(j - 1).to;
                    int nex_node = r.tasks.get(j).from;

                    // get the insertion cost
                    // when add into route i at index j
                    int change = t.dist - data.dist[pre_node][nex_node] + (data.dist[pre_node][t.from] + data.dist[t.to][nex_node]);

                    // update first_cost second_cost
                    if (change < first_cost) {
                        second_cost = first_cost;
                        first_cost = change;
                        best_route = i;
                        best_index = j;
                    }
                    if (change > first_cost && change < second_cost) second_cost = change;
                }
            }

            // after travelling all routes
            if (best_route >= 0) {
                regret.add(second_cost - first_cost);
                insert_route.add(best_route);
                insert_index.add(best_index);
                tasks.add(t);
            }
            if (need_new_route) {
                regret.add(0);
                insert_route.add(s.routes.size());
                insert_index.add(1);
                tasks.add(t);
            }
        }

    }
}
