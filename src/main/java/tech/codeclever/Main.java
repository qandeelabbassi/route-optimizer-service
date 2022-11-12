package tech.codeclever;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.codeclever.structure.Data;
import tech.codeclever.structure.Solution;
import tech.codeclever.util.MyParameter;
import tech.codeclever.util.ReadData;
import tech.codeclever.util.WriteData;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Handler.class);
    public static void main(String[] args) {
        if (args.length % 2 != 0) {
            System.out.println("wrong args!");
            System.exit(1);
        } else for (int i = 0; i < args.length; i = i + 2){
            config(args[i], args[i + 1]);
            logger.info("CONFIG KEY: " + args[i]);
            logger.info("CONFIG VALUE: " + args[i + 1]);
        }

        Algorithm algo = new Algorithm();
        try {
            Data[] all_data = {ReadData.get(MyParameter.DATA_PATH)};
            for (Data data : all_data) {
                data.show();
                data.preprocess();
                MyParameter.init(data);
                Solution sol = algo.run(data);
                WriteData.write(sol, data);
//                System.out.println(sol);
//                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void config(String type, String arg) {
        switch (type) {
            case "-s":
                if (arg.equals("time")) MyParameter.setRandomSeed();
                else MyParameter.setRandomSeed(Integer.parseInt(arg));
                break;
            case "-t":
                MyParameter.setRunningTime(Double.parseDouble(arg));
                break;
            case "-ls":
                switch (arg) {
                    case "order":
                        MyParameter.setLSType(0);
                        break;
                    case "best":
                        MyParameter.setLSType(1);
                        break;
                    default: {
                        System.out.println("no such para for ls");
                        System.exit(1);
                    }
                }
                break;
            case "-p":
                MyParameter.setDataPath(arg);
                break;
            default:
                System.out.println("no such para");
                System.exit(1);
        }
    }
}
