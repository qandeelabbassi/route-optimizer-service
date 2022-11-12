package tech.codeclever;

import tech.codeclever.structure.Data;
import tech.codeclever.structure.Solution;
import tech.codeclever.structure.Task;
import tech.codeclever.util.Destroy_and_Repair_Pool;
import tech.codeclever.util.LS_Operator_Pool;
import tech.codeclever.util.MyParameter;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Algorithm {

    private final Random random = MyParameter.random;
    private Data data;
    private double start_time;

    private int destructor_selected;
    private int constructor_selected;
    private int k_max;
    private int KickCountdown;
    private Solution x_incumbent;
    private Solution x_localIncumbent;
    private Solution x_current;
    private Solution x_BestThisStage;
    private final Initialization opt_init = new Initialization();
    private final LS_Operator_Pool opt_LS = new LS_Operator_Pool();
    private final Destroy_and_Repair_Pool opt_des_con = new Destroy_and_Repair_Pool();

    public Solution run(Data data) {
        this.data = data;
        AILS();
        return x_incumbent;
    }

    private void AILS() {
        //  Initialize global variables
        int iterPerStage = MyParameter.N_ITER_PER_STAGE;
        k_max = MyParameter.k_max;
        KickCountdown = MyParameter.ITER_BEFORE_KICK;
        Initialize_Roulette_Probabilities();

        // Construct first solution and take to deep local optimum
        Solution x_init = opt_init.articleWay(data);

        x_incumbent = opt_LS.LS_Full(x_init);
        x_localIncumbent = new Solution(x_incumbent);

        // Main body: iterative phase
        start_time = System.nanoTime();

        do {

            x_current = new Solution(x_localIncumbent);
            x_BestThisStage = new Solution(x_localIncumbent);

            // Execute a batch of intensifying iterations—a stage
            int IterationCounter = 0;
            while (IterationCounter < iterPerStage) {
                IterationCounter += 1;
                boolean NewStage = Combined_ALNS_and_LS();
                if (NewStage) {
                    iterPerStage = MyParameter.N_ITER_PER_STAGE - 1;
                    KickCountdown = MyParameter.ITER_BEFORE_KICK;
                    IterationCounter = iterPerStage;
                }
            }

            // Increase number of iterations
            iterPerStage += 1;
        } while (TimeOut());

    }

    private boolean TimeOut() {
        double end_time = System.nanoTime();
        return (end_time - start_time) / 1e9 < MyParameter.running_time;
    }

    private boolean Combined_ALNS_and_LS() {
        // Returns TRUE if a new stage is required, FALSE otherwise
        // Check for stagnation
        if (KickCountdown > 0) {
            int k = random.nextInt(k_max) + 1;
            x_current = Roulette_Destroy_and_Repair(k);
            if (x_current.getDist() == x_BestThisStage.getDist()) return false;

            // Intensify with LS_Full, LS1, or LS2 based on random choice and instance size
            if (MyParameter.tho_LS_full > random.nextDouble())
                opt_LS.LS_Full(x_current);
            else {
                if ((data.total_requests < 200)) opt_LS.LS_1(x_current);
                else opt_LS.LS_2(x_current);
            }
            KickCountdown -= 1;
            if (x_current.getDist() < x_BestThisStage.dist) {
                x_BestThisStage = x_current; // this can be delete
                // Give higher probability to selected Destructor/Constructor pair
                Update_Roulette_Probabilities();
                Update_Incumbents(x_current);
                return true;
            }
            return false;
        } else {
            // Nothing has happened for a while, make a major, random destroy and repair
            int k = random.nextInt(data.total_requests - data.total_requests / 2)
                    + data.total_requests / 2;
            x_localIncumbent = Random_Destroy_and_Repair(k);
            x_localIncumbent = opt_LS.LS_Full(x_localIncumbent);
            x_current = x_localIncumbent;
            x_current.getDist();
            Update_Incumbents(x_current);
            return true;
        }
    }

    /**
     * call to make a kick after a certain amount of search effort has been performed
     * without acceptance of a new current solution.
     * It calls the random-destructor and then the random-constructor
     * for a number of tasks randomly drawn k value
     */
    private Solution Random_Destroy_and_Repair(int k) {
        List<Task> removed = opt_des_con.random_destroy.destruct(data, k, x_localIncumbent);
        opt_des_con.random_repair.construct(removed, x_localIncumbent);
        return x_localIncumbent;
    }

    /**
     * calls the normal, roulette wheel-based selection of
     * a destructor and constructor pair
     * and the execution of these operators
     * with a randomly drawn k value
     */
    private Solution Roulette_Destroy_and_Repair(int k) {
        int sum = 0;
        int total_pairs = opt_des_con.pi.length * opt_des_con.pi[0].length;
        int[] accumulateSum = new int[total_pairs];
        for (int i = 0; i < opt_des_con.pi.length; i++) {
            for (int j = 0; j < opt_des_con.pi[0].length; j++) {
                sum += opt_des_con.pi[i][j];
                accumulateSum[i * opt_des_con.pi[0].length + j] = sum;
            }
        }
        double prob = random.nextDouble() * sum;
        int chosen_pair = -1;
        for (int i = 0; i < total_pairs; i++) {
            if (i != 0) {
                if (prob < accumulateSum[i] && prob >= accumulateSum[i - 1]) {
                    chosen_pair = i;
                    break;
                }
            } else {
                if (prob < accumulateSum[i]) {
                    chosen_pair = i;
                    break;
                }
            }
        }
        destructor_selected = chosen_pair / opt_des_con.pi[0].length;
        constructor_selected = chosen_pair % opt_des_con.pi[0].length;
        opt_des_con.destroy_repair(data, destructor_selected, constructor_selected, k, x_current);

        return x_current;
    }

    /**
     * increases the probability of selecting a successful destructor/constructor pair
     */
    private void Update_Roulette_Probabilities() {
        opt_des_con.pi[destructor_selected][constructor_selected] += 1;
    }

    private void Update_Incumbents(Solution x_current) {
        if (x_current.dist < x_incumbent.dist) x_incumbent = new Solution(x_current);
    }

    private void Initialize_Roulette_Probabilities() {
        for (int[] row : opt_des_con.pi) Arrays.fill(row, 1);
    }
}
