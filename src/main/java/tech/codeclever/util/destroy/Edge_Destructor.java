package tech.codeclever.util.destroy;

import tech.codeclever.structure.*;

import java.util.ArrayList;
import java.util.List;

public class Edge_Destructor implements Destructor {

    //  If k ≤ Er , k random edge tasks are removed from the solution,
    //  otherwise the random-destructor is used.

    @Override
    public List<Task> destruct(Data data, int k, Solution sol) {

        if (k > data.edges_r)
            return new Random_Destructor().destruct(data, k, sol);

        List<Task> removed = new ArrayList<>();

        for (int i = 0; i < k; i++) {
            int remove_edge_index = random.nextInt(k - i) + 1;
            for (Route r : sol.routes) {
                if (remove_edge_index <= r.edges) {
                    for (int j = 1; j < r.tasks.size() - 1; j++) {
                        if (r.tasks.get(j).type == TaskType.EDGE) {
                            remove_edge_index--;
                            if (remove_edge_index == 0) {
                                removed.add(r.tasks.get(j));
                                r.remove(j);
                                break;
                            }
                        }
                    }
                    break;
                }
                remove_edge_index -= r.edges;
            }
        }
        sol.emptyRouteCheck();

        return removed;
    }
}
