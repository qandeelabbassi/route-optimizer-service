package tech.codeclever.util;

import tech.codeclever.structure.Solution;
import tech.codeclever.util.local_search.*;

public class LS_Operator_Pool {

    // here, two way to implement
    // 1. run all operator one by one
    // 2. every time we select the best operator among them to run
    // the second is easier to implement
    // here, we use the second way,
    // and we still offer possibility of the first way in our operators
    // (by "get_best_move" and "do_move" methods and "best_move_saving" attribute

    private final Operator flip = new Flip();
    private final Operator swap = new Swap();
    private final Operator or_opt = new Or_opt();
    private final Operator two_opt = new Two_opt();
    private final Operator three_opt = new Three_opt();

    private final Operator[] ls_full = new Operator[]{or_opt, swap, two_opt, three_opt, flip};
    private final Operator[] ls_1 = new Operator[]{swap, two_opt, three_opt, flip};
    private final Operator[] ls_2 = new Operator[]{or_opt, swap, two_opt};

    public Solution LS_Full(Solution sol) {
        ((Or_opt) or_opt).set_l(3);
        ((Three_opt) three_opt).set_Max_B_length(3);
        switch (MyParameter.LS_type) {
            case 0: {
                order(ls_full, sol);
                break;
            }
            case 1:{
                best(ls_full, sol);
                break;
            }
        }
        return sol;
    }

    public void LS_1(Solution sol) {
        ((Three_opt) three_opt).set_Max_B_length(1);
        switch (MyParameter.LS_type) {
            case 0: {
                order(ls_1, sol);
                break;
            }
            case 1: {
                best(ls_1, sol);
                break;
            }
        }
    }

    public void LS_2(Solution sol) {
        ((Or_opt) or_opt).set_l(2);
        switch (MyParameter.LS_type) {
            case 0:{
                order(ls_2, sol);
                break;
            }
            case 1:{
                best(ls_2, sol);
                break;
            }
        }
    }

    private void order(Operator[] ops, Solution s) {
        for (Operator op : ops) op.local_search(s);
    }

    private void best(Operator[] ops, Solution s){
        Operator bestOne;
        do {
            bestOne = null;
            int bestChange = 0;
            for (Operator op : ops) {
                if (op.get_best_move(s)) {
                    if (op.best_move_saving < bestChange) {
                        bestOne = op;
                        bestChange = op.best_move_saving;
                    }
                }
            }
            if (bestOne != null) bestOne.do_move(s);
        } while (bestOne != null);
    }
}
