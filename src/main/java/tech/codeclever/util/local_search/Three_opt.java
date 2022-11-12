package tech.codeclever.util.local_search;

import tech.codeclever.structure.*;

import java.util.ArrayList;
import java.util.List;

public class Three_opt extends Operator {

    // intra move, we see the route a line not a circle
    // if we wanna see it as a circle, we can split the route into 4 pieces
    // and two parts connected to depot have the same move

    //  breaks a route cycle in three segments A, B, and C,
    //  and reconnects them in all possible ways,
    //  also allowing reversals.

    private enum subType {
        ACB_, AC_B, AC_B_, A_CB, A_CB_, A_C_B, A_C_B_
    }

    private int max_B_length;
    private int selected_route_1;
    private int selected_route_2;
//    private int selected_cut_1;
//    private int selected_cut_2;
    private int selected_B;
    private Route best_new_1;
    private Route best_new_2;
//    private subType selected_type;

    public Three_opt() {
        this.max_B_length = 3;
    }

    public Three_opt(int max_B_length) {
        this.max_B_length = max_B_length;
    }

    @Override
    public boolean get_best_move(Solution s) {
        Data data = s.data;
        selected_route_1 = -1;
        selected_route_2 = -1;
//        selected_cut_1 = -1;
//        selected_cut_2 = -1;
        selected_B = -1;
        best_move_saving = Integer.MAX_VALUE;
        for (int _r1 = 0; _r1 < s.routes.size(); _r1++) {
            Route r1 = s.routes.get(_r1);
            for (int b_length_1 = 1; b_length_1 <= max_B_length; b_length_1++) {
                // for every length of B
                for (int _t1 = 2; _t1 < r1.tasks.size() - 2; _t1++) {
                    if (_t1 + b_length_1 > r1.tasks.size() - 2) continue;
                    // for the first route
                    assert _t1 + b_length_1 < r1.tasks.size() - 1;
                    intra_move(data, r1, _r1, _t1, b_length_1);
                    for (int _r2 = 0; _r2 < s.routes.size(); _r2++) {
                        if (_r1 == _r2) continue;
                        Route r2 = s.routes.get(_r2);
                        // for every length of B
                        for (int _t2 = 1; _t2 < r2.tasks.size(); _t2++) {
                            // for the second route
                            inter_move(data, r1, _r1, _t1, b_length_1, r2, _r2, _t2);
                        }
                    }
                }
            }
        }
        return selected_route_1 >= 0;
    }

    @Override
    public void do_move(Solution s) {
        if (selected_route_1 == selected_route_2) {
//            System.out.println("old route => " + s.routes.get(selected_route_1));
            s.remove(selected_route_1);
//            System.out.println("intra 3-opt type " + selected_type
//                    + " => route[" + selected_route_1 + "] cut@[" + selected_cut_1
//                    + "] and cut@[" + (selected_cut_1 + selected_B) + ']');

//            System.out.println("new route => " + best_new_1);
        } else {
//            System.out.println("old route_1 => " + s.routes.get(selected_route_1));
//            System.out.println("old route_2 => " + s.routes.get(selected_route_2));
            if (selected_route_1 < selected_route_2) {
                s.remove(selected_route_2);
                s.remove(selected_route_1);
            } else {
                s.remove(selected_route_1);
                s.remove(selected_route_2);
            }
            s.add(best_new_2);
//            System.out.println("inter 3-opt type " + selected_type
//                    + " => route[" + selected_route_1 + "] cut@[" + selected_cut_1
//                    + "] and cut@[" + (selected_cut_1 + selected_B) + ']');
//            System.out.println("inter 3-opt type " + selected_type
//                    + " => route[" + selected_route_2 + "] cut@[" + selected_cut_2
//                    + "]");
//            System.out.println("new route_1 => " + best_new_1);
//            System.out.println("new route_2 => " + best_new_2);
        }
        s.add(best_new_1);

//        System.out.println();

    }

    private void intra_move(Data data, Route r, int selected_route_index,
                            int selected_cut_index, int selected_B_length) {
        List<Task> A = new ArrayList<>();
        List<Task> B = new ArrayList<>();
        List<Task> C = new ArrayList<>();
        get_segment(r, 1, selected_cut_index, A);
        get_segment(r, selected_cut_index, selected_cut_index + selected_B_length, B);
        get_segment(r, selected_cut_index + selected_B_length, r.tasks.size() - 1, C);
        assert (A.size() + B.size() + C.size()) == r.tasks.size() - 2;
        for (subType sub_type : subType.values()) {
            if (sub_type == subType.AC_B_) continue;
            Route newR = new Route(data);
            switch (sub_type) {
                // ACB_, AC_B, A_CB, A_CB_, A_C_B, A_C_B_
                case ACB_: 
                    complete_route(data, newR, A, false, C, false, B, true);
                    break;
                case AC_B: 
                    complete_route(data, newR, A, false, C, true, B, false);
                    break;
                case A_CB: 
                    complete_route(data, newR, A, true, C, false, B, false);
                    break;
                case A_CB_: 
                    complete_route(data, newR, A, true, C, false, B, true);
                    break;
                case A_C_B: 
                    complete_route(data, newR, A, true, C, true, B, false);
                    break;
                case A_C_B_: 
                    complete_route(data, newR, A, true, C, true, B, true);
                    break;
            }
            int change = newR.dist - r.dist;
            if (change < 0 && change < best_move_saving) {
                selected_route_1 = selected_route_2 = selected_route_index;
//                selected_cut_1 = selected_cut_2 = selected_cut_index;
                selected_B = selected_B_length;
                best_move_saving = change;
//                selected_type = sub_type;
                best_new_1 = best_new_2 = new Route(newR);
            }
        }
    }

    private void inter_move(Data data, Route r1, int selected_route_index_1,
                            int selected_cut_index_1, int selected_B_length_1,
                            Route r2, int selected_route_index_2,
                            int selected_cut_index_2) {

        //    __0  1__2  3___     route 1
        //   /               \
        //  1                1
        //   \____4    5____/     route 2

        List<Task> A = new ArrayList<>();
        List<Task> B = new ArrayList<>();
        List<Task> C = new ArrayList<>();
        int A_d = get_segment(r1, 1, selected_cut_index_1, A);
        int B_d = get_segment(r1, selected_cut_index_1, selected_cut_index_1 + selected_B_length_1, B);
        int C_d = get_segment(r1, selected_cut_index_1 + selected_B_length_1, r1.tasks.size() - 1, C);
        assert A.size() + B.size() + C.size() == r1.tasks.size() - 2;
        List<Task> D = new ArrayList<>();
        List<Task> E = new ArrayList<>();
        int D_d = get_segment(r2, 1, selected_cut_index_2, D);
        int E_d = get_segment(r2, selected_cut_index_2, r2.tasks.size() - 1, E);
        assert D.size() + E.size() == r2.tasks.size() - 2;
        if (D_d + E_d + B_d > data.max_capacity) return;
        for (subType sub_type : subType.values()) {
            Route newR1 = new Route(data);
            Route newR2 = new Route(data);
            switch (sub_type) {
                case ACB_: {
                    complete_route(data, newR1, A, false, C, false, null, false);
                    complete_route(data, newR2, D, false, B, true, E, false);
                    break;
                }
                case AC_B : {
                    complete_route(data, newR1, A, false, C, true, null, false);
                    complete_route(data, newR2, D, false, B, false, E, true);
                    break;
                }
                case AC_B_ : {
                    complete_route(data, newR1, A, false, C, true, null, false);
                    complete_route(data, newR2, D, false, B, true, E, true);
                    break;
                }
                case A_CB : {
                    complete_route(data, newR1, A, true, C, false, null, false);
                    complete_route(data, newR2, D, true, B, false, E, false);
                    break;
                }
                case A_CB_ : {
                    complete_route(data, newR1, A, true, C, false, null, false);
                    complete_route(data, newR2, D, true, B, true, E, false);
                    break;
                }
                case A_C_B : {
                    complete_route(data, newR1, A, true, C, true, null, false);
                    complete_route(data, newR2, D, true, B, false, E, true);
                    break;
                }
                case A_C_B_ : {
                    complete_route(data, newR1, A, true, C, true, null, false);
                    complete_route(data, newR2, D, true, B, true, E, true);
                    break;
                }
            }
            int change = newR1.dist + newR2.dist - r1.dist - r2.dist;
            if (change < 0 && change < best_move_saving) {
                selected_route_1 = selected_route_index_1;
                selected_route_2 = selected_route_index_2;
//                selected_cut_1 = selected_cut_index_1;
//                selected_cut_2 = selected_cut_index_2;
                selected_B = selected_B_length_1;
                best_move_saving = change;
//                selected_type = sub_type;
                best_new_1 = new Route(newR1);
                best_new_2 = new Route(newR2);
            }
        }
    }

    private void complete_route(Data data, Route r,
                                List<Task> A, boolean reverseA,
                                List<Task> B, boolean reverseB,
                                List<Task> C, boolean reverseC) {
        add_list(data, A, r, reverseA);
        add_list(data, B, r, reverseB);
        add_list(data, C, r, reverseC);
        r.add(data.depot);
    }

    private void add_list(Data data, List<Task> list, Route r, boolean reverse) {

        if (list == null) return;
        if (!reverse) for (Task t : list) r.add(t);
        else {
            for (int i = list.size() - 1; i >= 0; i--) {
                Task t = list.get(i);
                if (t.type == TaskType.EDGE) t = data.get_reverse_edge(t);
                r.add(t);
            }
        }
    }

    private int get_segment(Route r, int start, int end, List<Task> segment) {
        int total_demand = 0;
        for (int i = start; i < end; i++) {
            segment.add(r.tasks.get(i));
            total_demand += r.tasks.get(i).demand;
        }
        return total_demand;
    }

    public void set_Max_B_length(int max_B_length) { this.max_B_length = max_B_length; }
}
