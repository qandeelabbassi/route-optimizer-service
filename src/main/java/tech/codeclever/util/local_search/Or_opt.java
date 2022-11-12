package tech.codeclever.util.local_search;

import tech.codeclever.structure.Data;
import tech.codeclever.structure.Route;
import tech.codeclever.structure.Solution;
import tech.codeclever.structure.Task;

import java.util.ArrayList;
import java.util.List;

public class Or_opt extends Operator {

    //  Or-opt relocates a segment of tasks,
    //  either within a route, or from one route to another.
    //  The length of the segment to be relocated is limited to l tasks.

    // do not consider the reversal

    private int l;
    private int route_remove;
    private int route_insert;
    private int index_remove;
    private int index_insert;

    public Or_opt() {
        this.l = 3;
    }

    public Or_opt(int l) {
        this.l = l;
    }

    @Override
    public void local_search(Solution s) {
        while (get_best_move(s)) {
            do_move(s);
            s.emptyRouteCheck();
        }
    }

    @Override
    public boolean get_best_move(Solution s) {
        Data data = s.data;
        route_remove = -1;
        route_insert = -1;
        index_remove = -1;
        index_insert = -1;
        best_move_saving = Integer.MAX_VALUE;
        for (int r_1 = 0; r_1 < s.routes.size(); r_1++) {
            Route r_remove = s.routes.get(r_1);
            // for every segment
            for (int t_1 = 1; t_1 < r_remove.tasks.size() - l; t_1++) {
                for (int r_2 = 0; r_2 < s.routes.size(); r_2++) {
                    Route r_insert = s.routes.get(r_2);
                    // for every possible insertion pos
                    for (int t_2 = 1; t_2 < r_insert.tasks.size() - 1; t_2++) {
                        // insertion pos is in the remove task
                        if (r_1 == r_2 && t_1 <= t_2 && t_1 >= t_2 - l) continue;
                        if (r_1 != r_2) {
                            // calc demand
                            int demand_change = 0;
                            List<Task> task_list = r_remove.tasks.subList(t_1, t_1 + l);
                            for (Task t : task_list) demand_change += t.demand;
                            if (r_insert.load + demand_change > data.max_capacity)
                                continue;
                        }
                        // calc change
                        int change = 0;
                        int pre_remove = r_remove.tasks.get(t_1 - 1).to;
                        int next_remove = r_remove.tasks.get(t_1 + l).from;
                        int pre_insert = r_insert.tasks.get(t_2 - 1).to;
                        int next_insert = r_insert.tasks.get(t_2).from;
                        int list_head = r_remove.tasks.get(t_1).from;
                        int list_tail = r_remove.tasks.get(t_1 + l - 1).to;
                        // do not need to consider the cost of segment
                        // when remove
                        change += (data.dist[pre_remove][next_remove] - data.dist[pre_remove][list_head] - data.dist[list_tail][next_remove]);
                        // when add
                        change += (data.dist[pre_insert][list_head] + data.dist[list_tail][next_insert] - data.dist[pre_insert][next_insert]);
                        if (change < 0 && change < best_move_saving) {
                            route_remove = r_1;
                            route_insert = r_2;
                            index_remove = t_1;
                            index_insert = t_2;
                            best_move_saving = change;
                        }
                    }
                }
            }
        }
        return route_remove >= 0;
    }

    @Override
    public void do_move(Solution s) {
        List<Task> task_list = new ArrayList<>();
        while (task_list.size() < l) task_list.add(s.routes.get(route_remove).remove(index_remove));
//        System.out.println("tasks => " + task_list + " from route[" + route_remove
//                + "] move to route[" + route_insert + "] at pos[" + index_insert + ']');
        if (route_remove != route_insert) s.routes.get(route_insert).add(task_list, index_insert);
        else {
            assert index_insert < index_remove || index_insert > index_remove + l;
            if (index_insert < index_remove) s.routes.get(route_insert).add(task_list, index_insert);
            else if (index_insert > index_remove + l) s.routes.get(route_insert).add(task_list, index_insert - l);
        }
    }

    public void set_l(int l) {
        this.l = l;
    }
}
