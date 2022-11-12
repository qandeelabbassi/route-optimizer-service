package tech.codeclever.util.destroy;

import tech.codeclever.structure.*;

import java.util.ArrayList;
import java.util.List;

public class Node_Destructor implements Destructor {

    //  If k ≤ Nr , k random node tasks are removed from the solution,
    //  otherwise the random-destructor is used.

    @Override
    public List<Task> destruct(Data data, int k, Solution sol) {

        if (k > data.nodes_r)
            return new Random_Destructor().destruct(data, k, sol);

        List<Task> removed = new ArrayList<>();

        for (int i = 0; i < k; i++) {
            int remove_node_index = random.nextInt(k - i) + 1;
            for (Route r : sol.routes) {
                if (remove_node_index <= (r.nodes - 2)) {
                    for (int j = 1; j < r.tasks.size() - 1; j++) {
                        if (r.tasks.get(j).type == TaskType.NODE) {
                            remove_node_index--;
                            if (remove_node_index == 0) {
                                removed.add(r.tasks.get(j));
                                r.remove(j);
                                break;
                            }
                        }
                    }
                    break;
                }
                remove_node_index -= (r.nodes - 2);
            }
        }
        sol.emptyRouteCheck();

        return removed;
    }
}
