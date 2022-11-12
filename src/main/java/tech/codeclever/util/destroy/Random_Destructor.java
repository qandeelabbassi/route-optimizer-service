package tech.codeclever.util.destroy;

import tech.codeclever.structure.Data;
import tech.codeclever.structure.Route;
import tech.codeclever.structure.Solution;
import tech.codeclever.structure.Task;

import java.util.ArrayList;
import java.util.List;

public class Random_Destructor implements Destructor {

    // k random tasks are selected and removed from the solution

    @Override
    public List<Task> destruct(Data data, int k, Solution sol) {

        List<Task> removed_tasks = new ArrayList<>();

        for (int i = 0; i < k; i++) {
            int the_task_index = random.nextInt(data.total_requests - i) + 1;
            for (Route r : sol.routes) {
                if (the_task_index <= r.tasks.size() - 2) {
                    removed_tasks.add(r.remove(the_task_index));
                    break;
                }
                the_task_index -= (r.tasks.size() - 2);
            }
        }
        sol.emptyRouteCheck();

        return removed_tasks;
    }
}
