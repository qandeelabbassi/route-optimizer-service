package tech.codeclever.util.destroy;

import tech.codeclever.structure.*;

import java.util.ArrayList;
import java.util.List;

public class Arc_Destructor implements Destructor {

    //  If k ≤ Ar , k random arc tasks are removed from the solution,
    //  otherwise the random destructor is used.

    @Override
    public List<Task> destruct(Data data, int k, Solution sol) {
        if (k > data.arcs_r)
            return new Random_Destructor().destruct(data, k, sol);

        List<Task> removed = new ArrayList<>();

        for (int i = 0; i < k; i++) {
            int remove_arc_index = random.nextInt(k - i) + 1;
            for (Route r : sol.routes) {
                if (remove_arc_index <= r.arcs) {
                    for (int j = 1; j < r.tasks.size() - 1; j++) {
                        if (r.tasks.get(j).type == TaskType.ARC) {
                            remove_arc_index--;
                            if (remove_arc_index == 0) {
                                removed.add(r.tasks.get(j));
                                r.remove(j);
                                break;
                            }
                        }
                    }
                    break;
                }
                remove_arc_index -= r.arcs;
            }
        }
        sol.emptyRouteCheck();

        return removed;
    }
}
