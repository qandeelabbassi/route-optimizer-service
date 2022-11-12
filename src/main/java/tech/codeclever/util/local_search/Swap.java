package tech.codeclever.util.local_search;

import tech.codeclever.structure.*;

public class Swap extends Operator {

    //  Swap exchanges the position of two tasks
    //  (both intra-route and inter-route).

    //  here, do not consider the directions of edges

    private int route_1;
    private int route_2;
    private int index_1;
    private int index_2;

    @Override
    public boolean get_best_move(Solution s) {
        Data data = s.data;
        route_1 = -1;
        route_2 = -1;
        index_1 = -1;
        index_2 = -1;
        best_move_saving = Integer.MAX_VALUE;
        for (int i_1 = 0; i_1 < s.routes.size(); i_1++) {
            Route r1 = s.routes.get(i_1);
            for (int j_1 = 1; j_1 < r1.tasks.size() - 1; j_1++) {
                for (int i_2 = i_1; i_2 < s.routes.size(); i_2++) {
                    Route r2 = s.routes.get(i_2);
                    for (int j_2 = 1; j_2 < r2.tasks.size() - 1; j_2++) {
                        if (i_1 == i_2 && j_2 <= j_1) continue;
                        // for every task,
                        // except the one has been visited
                        Task t1 = r1.tasks.get(j_1);
                        Task t2 = r2.tasks.get(j_2);
                        if (r1.load - t1.demand + t2.demand > data.max_capacity
                                || r2.load - t2.demand + t1.demand > data.max_capacity) continue;
                        int change = 0;
                        // get the pre and next of t1
                        int pre_node_1 = r1.tasks.get(j_1 - 1).to;
                        int nex_node_1 = r1.tasks.get(j_1 + 1).from;
                        // get the pre and next of t2
                        int pre_node_2 = r2.tasks.get(j_2 - 1).to;
                        int nex_node_2 = r2.tasks.get(j_2 + 1).from;
                        if (i_1 == i_2 && j_2 == j_1 + 1) change = getChangeForAdjacentTask(data, t1, t2, change, pre_node_1, nex_node_2);
                        else {
                            // change - remove dist
                            // change + add dist
                            change = getChangeForNotAdjacentTask(data, t1, change, pre_node_1, nex_node_1, pre_node_2, nex_node_2);
                            change = getChangeForNotAdjacentTask(data, t2, change, pre_node_2, nex_node_2, pre_node_1, nex_node_1);
                        }
                        // if we find a better move, replace current move
                        if (change < 0 && change < best_move_saving) {
                            route_1 = i_1;
                            route_2 = i_2;
                            index_1 = j_1;
                            index_2 = j_2;
                            best_move_saving = change;
                        }
                    }
                }
            }
        }
        // if route_1 < 0, it means we cannot find a valid move
        return route_1 >= 0;
    }

    private int getChangeForAdjacentTask(Data data, Task t1, Task t2, int change, int pre, int next) {
        change -= (data.dist[pre][t1.from] + data.dist[t1.to][t2.from] + data.dist[t2.to][next]);
        change += (data.dist[pre][t2.from] + data.dist[t2.to][t1.from] + data.dist[t1.to][next]);
        return change;
    }

    private int getChangeForNotAdjacentTask(Data data, Task t, int change,
                                            int pre_node_remove, int nex_node_remove,
                                            int pre_node_insert, int nex_node_insert) {
        change -= (data.dist[pre_node_remove][t.from] + data.dist[t.to][nex_node_remove]);
        change += (data.dist[pre_node_insert][t.from] + data.dist[t.to][nex_node_insert]);
        return change;
    }

    @Override
    public void do_move(Solution s) {
        assert route_1 != route_2 || index_2 > index_1;
        Task t2 = s.routes.get(route_2).remove(index_2);
        // removing t2 will not affect removing t1 ( t2 > t1 )
        Task t1 = s.routes.get(route_1).remove(index_1);
        s.routes.get(route_1).add(t2, index_1);
        s.routes.get(route_2).add(t1, index_2);
//        System.out.println("change " + t2 + " and " + t1 + " saving => " + best_move_saving);
    }
}
