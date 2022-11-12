package tech.codeclever.util.repair;

import tech.codeclever.structure.*;

import java.util.ArrayList;
import java.util.List;

public class Random_Constructor extends Constructor {

    //  Insert each task, one at a time,
    //  according to the order in which they have been removed
    //  from the solution by the destructor in a random position
    //  in the current set of routes.
    //  If no feasible position exists,
    //  create a new route with only this task.

    @Override
    public void construct(List<Task> remains, Solution sol) {
        Data data = sol.data;
        while (remains.size() > 0) {
            Task t = remains.get(0);
            if (!random_add(t, sol, data)) {
                Route r = new Route(data);
                r.add(t);
                r.add(data.depot);
                sol.add(r);
            }
            remains.remove(0);
        }
    }

    private boolean random_add(Task t, Solution s, Data data) {
        List<Route> rs = new ArrayList<>(s.routes);
        rs.removeIf(r -> (t.demand + r.load) > data.max_capacity);
        if (rs.size() == 0)
            return false;
        int rs_index = random.nextInt(rs.size());
        int t_index = random.nextInt(rs.get(rs_index).tasks.size() - 1) + 1;
        Task temp = t;
        if (t.type == TaskType.EDGE && random.nextBoolean()) {
            temp = data.get_reverse_edge(t);
        }
        rs.get(rs_index).add(temp, t_index);
        return true;
    }
}
