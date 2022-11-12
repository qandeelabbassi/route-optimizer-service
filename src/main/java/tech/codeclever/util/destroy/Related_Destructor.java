package tech.codeclever.util.destroy;

import tech.codeclever.structure.Data;
import tech.codeclever.structure.Route;
import tech.codeclever.structure.Solution;
import tech.codeclever.structure.Task;
import tech.codeclever.util.MyParameter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Related_Destructor implements Destructor {

    //  Define the contiguity of two tasks r and t as
    //  tho(r,t) = beta * c_prime_{r,t} / max{c_prime_{s,u}}
    //           + gamma * |q(r)-q(t)| / max{q(s)}
    //           + delta(r,t)
    //  where beta = 0.75, gamma = 0.1 as recommended in the literature,
    //  c_prime_{r,t} is the minimum traversal cost between r and t,
    //  q(t) is the demand of task t,
    //  and delta_{r,t} takes value 1
    //  if r and t are in the same route in the current solution
    //  and 0 otherwise.
    //  We start by selecting a random task,
    //  and then we add k − 1 tasks in a greedy way
    //  by selecting the next task as the one
    //  with minimum distance from the last added task.

    @Override
    public List<Task> destruct(Data data, int k, Solution sol) {

        List<Task> removed = new ArrayList<>();
        HashSet<Task> set = get_k_tasks_set(data, k, sol);

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

    // delta_{r,t} = 1 if r and t are in the same route in the current solution
    //             = 0 otherwise
    // we can start at a random task, mark the route contains the task
    // start to travel all route and tasks in them
    // get the list of tho(r,t)
    // if we calculate all tho(r,t) -> o(n^2)
    // if we calculate tho(r,t) one by one -> o((k-1)*n)
    // when doing so,
    // 1. add r into set ( randomly selected )
    // 2. mark the min tho(r,t) and related task t
    // 3. add t into set, r = t, t = null
    // 4. mark the min tho(r,t) and related task t ( not equal to the tasks already in set )
    // 5. repeat k-1 times

    private HashSet<Task> get_k_tasks_set(Data data, int k, Solution s) {

        HashSet<Task> set = new HashSet<>();

        // select a task randomly
        int r_route_index = random.nextInt(s.routes.size());
        Route r_route = s.routes.get(r_route_index);
        int r_index = random.nextInt(r_route.tasks.size() - 2) + 1;
//        System.out.println("rindex: " +r_index);
        Task r = r_route.tasks.get(r_index);

        // 1.
        set.add(r);
        // repeat k-1 times
        for (int i = 1; i < k; i++) {
            // 2.
            int min_t_route_index = -1;
            double min_r_t_tho = Integer.MAX_VALUE;
            Task min_t_task = null;
            // for each route
            for (int t_route_index = 0; t_route_index < s.routes.size(); t_route_index++) {
                Route t_route = s.routes.get(t_route_index);
                // get delta
                int delta = t_route_index == r_route_index ? 1 : 0;
                // for each task
                for (int t_index = 1; t_index < t_route.tasks.size() - 1; t_index++) {
                    Task t = t_route.tasks.get(t_index);
                    // do not consider task already been chosen
                    if (set.contains(t))
                        continue;
                    int c_prime_r_t = get_c_prime_r_t(data, r, t);
                    // get tho
                    double tho = MyParameter.beta * (c_prime_r_t * 1.0 / data.max_min_travel_r_t)
                            + MyParameter.gamma * (Math.abs(r.demand - t.demand) * 1.0 / data.max_demand_t)
                            + delta;
                    // update min_tho, route_index, the_task t
                    if (tho < min_r_t_tho) {
                        min_r_t_tho = tho;
                        min_t_route_index = t_route_index;
                        min_t_task = t;
                    }
                }
            }
            // 3.
            r_route_index = min_t_route_index;
            r = min_t_task;
            set.add(r);
        }

        return set;
    }

    private int get_c_prime_r_t(Data data, Task r, Task t) {
        int res = -1;
//        System.out.println("loading instance " + r);
        switch (r.type) {
            case NODE:
            case ARC: {
                switch (t.type) {
                    case NODE:
                    case ARC:
                        res = data.dist[r.to][t.from];
                        break;
                    case EDGE:
                        res = Math.min(data.dist[r.to][t.from], data.dist[r.to][t.to]);
                        break;
                }
                break;
            }
            case EDGE: {
                switch (t.type) {
                    case NODE:
                    case ARC:
                        res = Math.min(data.dist[r.from][t.from], data.dist[r.to][t.from]);
                        break;
                    case EDGE:
                        res = Math.min(
                            Math.min(data.dist[r.from][t.from], data.dist[r.to][t.from]),
                            Math.min(data.dist[r.from][t.to], data.dist[r.to][t.to])
                        );
                        break;
                }
                break;
            }
        }
        return res;
    }
}
