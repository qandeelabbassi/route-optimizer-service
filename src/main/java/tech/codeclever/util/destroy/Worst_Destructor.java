package tech.codeclever.util.destroy;

import tech.codeclever.structure.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Worst_Destructor implements Destructor {

    //  We define the cost of removing a task t from the current solution x
    //  as f(t,x) = z(x) - z_{-t}(x),
    //  where z_{−t}(x) is the cost of the solution without task t.
    //  The operator removes the k tasks having the highest values of f(t,x)

    @Override
    public List<Task> destruct(Data data, int k, Solution sol) {
        HashSet<Task> set = get_k_tasks(data, k, sol);
        List<Task> removed = new ArrayList<>();
        for (Route r : sol.routes) {
            int i = 1;
            if (set.isEmpty())
                break;
            while (i < r.tasks.size() - 1) {
                Task t = r.tasks.get(i);
                if (set.contains(t)) {
                    removed.add(r.remove(i));
                    continue;
                }
                i++;
            }
        }
        sol.emptyRouteCheck();
        return removed;
    }

    private HashSet<Task> get_k_tasks(Data data, int k, Solution s) {
        // use a easy way,
        // calculate cost of each task and add into list -> o(n)
        // sort list -> o(n*log(n))
        // get the the highest k tasks into set
        List<Tuple<Task, Integer>> all_task_cost_pairs = new ArrayList<>();
        //  f(t,x) = z(x) - z_{-t}(x)
        for (Route r : s.routes) {
            for (int i = 1; i < r.tasks.size() - 1; i++) {
                Task t = r.tasks.get(i);
                int pre_node = r.tasks.get(i - 1).to;
                int nex_node = r.tasks.get(i + 1).from;
                int cost = t.dist - data.dist[pre_node][nex_node] + (data.dist[pre_node][t.from] + data.dist[t.to][nex_node]);
                all_task_cost_pairs.add(new Tuple<>(t, cost));
            }
        }
        all_task_cost_pairs.sort((o1, o2) -> o2.second.compareTo(o1.second));
        HashSet<Task> res = new HashSet<>();
        for (int i = 0; i < k; i++) res.add(all_task_cost_pairs.get(i).first);
        return res;
    }

}
