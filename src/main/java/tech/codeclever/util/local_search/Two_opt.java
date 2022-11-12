package tech.codeclever.util.local_search;

import tech.codeclever.structure.*;

import java.util.ArrayList;
import java.util.List;

public class Two_opt extends Operator {

    // intra move, we see the route a line not a circle
    // if we wanna see it as a circle, we can split the route into 3 pieces
    // and two parts connected to depot have the same move

    //  2-opt breaks a route cycle in two segments and reconnects the segments
    //  in the only possible way.
    //  We also adapted to the MCGRP the seven additional operator subtypes
    //  originally proposed for the CARP.

    /**
     * two route, 1 => a1,a2,a3,a4; 2 => b1,b2,b3,b4
     * *******intra********
     * _1:
     * a1,a3,a2,a4
     * *******inter********
     * _2:
     * a1,a2,b3,b4 + b1,b2,a3,a4
     * _3:
     * a1,a2,b3,b4 + a3,a4,b2,b1
     * _4:
     * a1,a2,b3,b4 + a4,a3,b1,b2
     * _5:
     * a1,a2,b2,b1 + a3,a4,b3,b4
     * _6:
     * a1,a2,b2,b1 + b3,b4,a3,a4
     * _7:
     * a1,a2,b4,b3 + b1,b2,a3,a4
     * _8:
     * a2,a1,b3,b4 + b1,b2,a3,a4
     */
    private enum subType {
        _1, _2, _3, _4, _5, _6, _7, _8
    }

    // we definitely need to explain the cut
    // A B C D E F G H
    // if we cut at 3
    // and it will become: A B C + D E F G H
    private int route_1;
    private int route_2;
    //    private int cut_1;
//    private int cut_2;
    private subType best_type;
    private Route best_new1;
    private Route best_new2;

    @Override
    public boolean get_best_move(Solution s) {
        Data data = s.data;
        route_1 = -1;
        route_2 = -1;
//        cut_1 = -1;
//        cut_2 = -1;
        best_move_saving = Integer.MAX_VALUE;
        for (int _r1 = 0; _r1 < s.routes.size(); _r1++) {
            Route r1 = s.routes.get(_r1);
            for (int _t1 = 1; _t1 < r1.tasks.size(); _t1++) {
                // for first route
                // _r2 start from 0,
                // cause in MCGRP, change direction may change the cost
                for (int _r2 = 0; _r2 < s.routes.size(); _r2++) {
                    Route r2 = s.routes.get(_r2);
                    for (int _t2 = 1; _t2 < r2.tasks.size(); _t2++) {
                        // for second route
                        // same route, we need _t2 > _t1
                        // at least, we test one task
                        if (_r1 == _r2 && _t2 <= _t1) continue;
                        // not same route, we avoid 1 and last
                        // cause exchanging the whole route is meaningless
                        if (_r1 != _r2 &&
                                (_t1 == 1 || _t1 == r1.tasks.size() - 1
                                        || _t2 == 1 || _t2 == r2.tasks.size() - 1)) continue;
                        // at same route
                        if (_r1 == _r2) intra_move(data, _r1, r1, _t1, _t2);
                            // at different routes
                        else inter_move(data, _r1, _r2, r1, r2, _t1, _t2);
                    }
                }
            }
        }
        return route_1 >= 0;
    }

    @Override
    public void do_move(Solution s) {
        switch (best_type) {
            case _1: 
                do_intra_move(s);
                break;
            case _2:
            case _3:
            case _4:
            case _5:
            case _6:
            case _7: 
            case _8: 
                do_inter_move(s, best_type);
                break;
        }
    }

    // do intra or inter move
    // if can find better move,
    // update route index, cut index, best_move_saving, best_type
    private void intra_move(Data data, int route_index, Route route,
                            int cut_first, int cut_second) {
//        List<Task> segment = new ArrayList<>();
//        Route temp = new Route(route);
//        for (int i = cut_first; i < cut_second; i++) segment.add(temp.remove(cut_first));
//        for (Task t : segment) {
//            if (t.type == TaskType.EDGE) t = Data.get_reverse_edge(t);
//            temp.add(t, cut_first);
//        }
        Route temp = new Route(data); // only travel once
        for (int i = 1; i < cut_first; i++) temp.add(route.tasks.get(i));
        for (int i = cut_second - 1; i >= cut_first; i--) {
            Task t = route.tasks.get(i);
            if (t.type == TaskType.EDGE) temp.add(data.get_reverse_edge(t));
            else temp.add(t);
        }
        for (int i = cut_second; i < route.tasks.size(); i++) temp.add(route.tasks.get(i));
        int change = temp.dist - route.dist;
        if (change < 0 && change < best_move_saving) {
            route_1 = route_2 = route_index;
//            cut_1 = cut_first;
//            cut_2 = cut_second;
            best_move_saving = change;
            best_type = subType._1;
//            best_new1 = best_new2 = new Route(temp);
            best_new1 = best_new2 = temp; // every time we use this method, temp will be new
//            System.out.println(best_new1);
        }
    }

    private void inter_move(Data data, int route_index_first, int route_index_second,
                            Route route_first, Route route_second,
                            int cut_first, int cut_second) {
        List<Task> a1a2 = new ArrayList<>();
        List<Task> a3a4 = new ArrayList<>();
        List<Task> b1b2 = new ArrayList<>();
        List<Task> b3b4 = new ArrayList<>();
        int a1a2_demand = get_inter_segment(route_first, a1a2, a3a4, cut_first);
        int a3a4_demand = route_first.load - a1a2_demand;
        int b1b2_demand = get_inter_segment(route_second, b1b2, b3b4, cut_second);
        int b3b4_demand = route_second.load - b1b2_demand;
        for (subType sub_type : subType.values()) {
            if (sub_type == subType._1) continue;
            Route new1 = new Route(data);
            Route new2 = new Route(data);
            switch (sub_type) {
                case _2: {
                    if (a1a2_demand + b3b4_demand > data.max_capacity || b1b2_demand + a3a4_demand > data.max_capacity)
                        continue;
                    complete_inter_new_route(data, a1a2, false, b3b4, false, new1);
                    complete_inter_new_route(data, b1b2, false, a3a4, false, new2);
                    break;
                }
                case _3 : {
                    if (a1a2_demand + b3b4_demand > data.max_capacity || b1b2_demand + a3a4_demand > data.max_capacity)
                        continue;
                    complete_inter_new_route(data, a1a2, false, b3b4, false, new1);
                    complete_inter_new_route(data, a3a4, false, b1b2, true, new2);
                    break;
                }
                case _4 : {
                    if (a1a2_demand + b3b4_demand > data.max_capacity || b1b2_demand + a3a4_demand > data.max_capacity)
                        continue;
                    complete_inter_new_route(data, a1a2, false, b3b4, false, new1);
                    complete_inter_new_route(data, a3a4, true, b1b2, false, new2);
                    break;
                }
                case _5 : {
                    if (a1a2_demand + b1b2_demand > data.max_capacity || a3a4_demand + b3b4_demand > data.max_capacity)
                        continue;
                    complete_inter_new_route(data, a1a2, false, b1b2, true, new1);
                    complete_inter_new_route(data, a3a4, false, b3b4, false, new2);
                    break;
                }
                case _6 : {
                    if (a1a2_demand + b1b2_demand > data.max_capacity || a3a4_demand + b3b4_demand > data.max_capacity)
                        continue;
                    complete_inter_new_route(data, a1a2, false, b1b2, true, new1);
                    complete_inter_new_route(data, b3b4, false, a3a4, false, new2);
                    break;
                }
                case _7 : {
                    if (a1a2_demand + b3b4_demand > data.max_capacity || b1b2_demand + a3a4_demand > data.max_capacity)
                        continue;
                    complete_inter_new_route(data, a1a2, false, b3b4, true, new1);
                    complete_inter_new_route(data, b1b2, false, a3a4, false, new2);
                    break;
                }
                case _8 : {
                    if (a1a2_demand + b3b4_demand > data.max_capacity || b1b2_demand + a3a4_demand > data.max_capacity)
                        continue;
                    complete_inter_new_route(data, a1a2, true, b3b4, false, new1);
                    complete_inter_new_route(data, b1b2, false, a3a4, false, new2);
                    break;
                }
            }
            int change = new1.dist + new2.dist - route_first.dist - route_second.dist;
            if (change < 0 && change < best_move_saving) {
                route_1 = route_index_first;
                route_2 = route_index_second;
//                cut_1 = cut_first;
//                cut_2 = cut_second;
                best_move_saving = change;
                best_type = sub_type;
//                best_new1 = new Route(new1);
//                best_new2 = new Route(new2);
                best_new1 = new1;
                best_new2 = new2; // every time we use this method, new1/2 will be new
            }
        }
    }

    private void do_intra_move(Solution s) {
        Route r = s.routes.remove(route_1);
//        System.out.println("old route => " + r);
//        List<Task> segment = new ArrayList<>();
//        for (int i = cut_1; i < cut_2; i++) {
//            segment.add(r.tasks.get(cut_1));
//            r.remove(cut_1);
//        }
//        System.out.println("intra 2-opt => route[" + route_1 + "] and tasks " + segment);
        s.routes.add(best_new1);
//        System.out.println("new route => " + best_new1);
//        System.out.println();
    }

    // how to implement?
    // just make two new routes!
    private void do_inter_move(Solution s, subType sub_type) {
        assert route_1 != route_2;
        Route r1, r2;
        if (route_1 < route_2) {
            r2 = s.remove(route_2);
            r1 = s.remove(route_1);
        } else {
            r1 = s.remove(route_1);
            r2 = s.remove(route_2);
        }
//        System.out.println("old route_1 => " + r1);
//        System.out.println("old route_2 => " + r2);
//        System.out.println("inter 2-opt type " + sub_type
//                + " => route[" + route_1 + "] cut@[" + cut_1
//                + "] and route[" + route_2 + "] cut@[" + cut_2 + ']');
        s.add(best_new1);
        s.add(best_new2);
//        System.out.println("new route_1 => " + best_new1);
//        System.out.println("new route_2 => " + best_new2);
//        System.out.println();
    }

    private void complete_inter_new_route(Data data, List<Task> A, boolean reverseA,
                                          List<Task> B, boolean reverseB,
                                          Route r) {
        add_inter_list(data, A, r, reverseA);
        add_inter_list(data, B, r, reverseB);
        r.add(data.depot);
    }

    private void add_inter_list(Data data, List<Task> list, Route r, boolean reverse) {
        if (!reverse) for (Task t : list) r.add(t);
        else {
            for (int i = list.size() - 1; i >= 0; i--) {
                Task t = list.get(i);
                if (t.type == TaskType.EDGE) t = data.get_reverse_edge(t);
                r.add(t);
            }
        }
    }

    private int get_inter_segment(Route r, List<Task> a1a2, List<Task> a3a4, int cut) {
        int first_list_demand = 0;
        for (int i = 1; i < cut; i++) {
            a1a2.add(r.tasks.get(i));
            first_list_demand += r.tasks.get(i).demand;
        }
        for (int i = cut; i < r.tasks.size() - 1; i++) a3a4.add(r.tasks.get(i));
        assert a1a2.size() + a3a4.size() == r.tasks.size() - 2;
        return first_list_demand;
    }
}
