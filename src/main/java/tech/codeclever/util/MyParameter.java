package tech.codeclever.util;

import tech.codeclever.structure.Data;

import java.util.Random;

public class MyParameter {

    public static Random random = new Random(1);
    public static String DATA_PATH = "";
    public static double running_time = 60;
    public static int N_ITER_PER_STAGE = 10;
    public static int k_max;
    public static double beta = 0.75;
    public static double gamma = 0.1;
    public static double tho_LS_full = 0.15;
    public static int LS_type = 0;
    public static int ITER_BEFORE_KICK;
    public static int BIG_NUM = 1000000;

    public static void setRandomSeed(int seed) {
        random = new Random(seed);
    }

    public static void setRandomSeed() {
        random = new Random();
    }

    public static void setLSType(int type) {
        LS_type = type;
    }

    public static void setRunningTime(double time) {
        running_time = time;
    }

    public static void setDataPath(String path) {
        DATA_PATH = path;
    }

    public static void init(Data data) {
        k_max = Math.min(50, data.total_requests - 2);
        ITER_BEFORE_KICK = 20000 * Math.max(1,
                20000 / (data.total_requests * data.total_requests
                        + data.arcs
                        + 2 * data.edges
                        + data.nodes * data.nodes / 5));
    }

}
