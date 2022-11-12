package tech.codeclever.util;

import tech.codeclever.structure.Data;
import tech.codeclever.structure.Solution;
import tech.codeclever.structure.Task;
import tech.codeclever.util.destroy.*;
import tech.codeclever.util.repair.Constructor;
import tech.codeclever.util.repair.Greedy_Constructor;
import tech.codeclever.util.repair.Random_Constructor;
import tech.codeclever.util.repair.Regret_Constructor;

import java.util.Arrays;
import java.util.List;

public class Destroy_and_Repair_Pool {

    public Destructor random_destroy = new Random_Destructor();
    public Destructor node_destroy = new Node_Destructor();
    public Destructor arc_destroy = new Arc_Destructor();
    public Destructor edge_destroy = new Edge_Destructor();
    public Destructor worst_destroy = new Worst_Destructor();
    public Destructor related_destroy = new Related_Destructor();
    public Destructor tree_destroy = new Tree_Destructor();
    public Destructor[] destructors = new Destructor[7];

    public Constructor random_repair = new Random_Constructor();
    public Constructor greedy_repair = new Greedy_Constructor();
    public Constructor regret_repair = new Regret_Constructor();
    public Constructor[] constructors = new Constructor[3];

    public int[][] pi = new int[7][3];

    public Destroy_and_Repair_Pool() {

        destructors[0] = random_destroy;
        destructors[1] = node_destroy;
        destructors[2] = arc_destroy;
        destructors[3] = edge_destroy;
        destructors[4] = worst_destroy;
        destructors[5] = related_destroy;
        destructors[6] = tree_destroy;
        constructors[0] = random_repair;
        constructors[1] = greedy_repair;
        constructors[2] = regret_repair;

        for (int[] pi_row : pi) {
            Arrays.fill(pi_row, 1);
        }
    }

    public void destroy_repair(Data data, int d_index, int r_index, int k, Solution s) {
        List<Task> removed = destructors[d_index].destruct(data, k, s);
        constructors[r_index].construct(removed, s);
    }

}
