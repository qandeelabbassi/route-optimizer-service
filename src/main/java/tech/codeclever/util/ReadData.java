package tech.codeclever.util;

import tech.codeclever.structure.*;

import java.io.*;
import java.util.*;

public class ReadData {

    public static File[] getFiles(String path) {
        List<File> files = new ArrayList<>();
        Queue<File> queue = new LinkedList<>();
        File f = new File(path);
        if (f.isDirectory())
            queue.offer(f);
        else files.add(f);
        while (!queue.isEmpty()) {
            File folder = queue.poll();
            File[] subs = folder.listFiles();
            for (File sub : subs) {
                if (sub.isDirectory()) queue.offer(sub);
                else files.add(sub);
            }
        }
        return files.toArray(new File[files.size()]);
    }

    public static Data[] getAll(String path) throws IOException {
        File[] files = getFiles(path);
        Data[] data = new Data[files.length];
        for (int i = 0; i < files.length; i++)
            data[i] = ReadData.get(files[i]);
        return data;
    }

    public static Data get(String path) throws IOException {
        File file = new File(path);
        return get(file);
    }

    public static Data get(File file) throws IOException {
        // basic info.
        Data data = new Data();
        Scanner scanner = new Scanner(file);
        scanner.next();
        data.name = scanner.next();
        extract(scanner, 2);
        System.out.println("loading instance " + data.name);
        data.max_vehicles = extract(scanner);
        data.max_capacity = extract(scanner);
        data.depot_node = extract(scanner, 2);
        data.nodes = extract(scanner);
        data.edges = extract(scanner);
        data.arcs = extract(scanner);
        data.nodes_r = extract(scanner, 2);
        data.edges_r = extract(scanner, 2);
        data.arcs_r = extract(scanner, 2);
        data.tasks = new Task[data.nodes_r + data.edges_r + data.arcs_r];
        // edge map
        data.edge_set = new HashMap<>();
        // task graph
        data.graph = new Task[data.nodes + 1][data.nodes + 1];
        // raw distance matrix
        data.raw_dist = new int[data.nodes + 1][data.nodes + 1];
        for (int[] a : data.raw_dist)
            Arrays.fill(a, MyParameter.BIG_NUM);
        scanner.next();
        scanner.nextLine();
        // required nodes
        for (int i = 0; i < data.nodes_r; i++)
            data.tasks[i] = extract(data, scanner, TaskType.NODE, false);
        scanner.next();
        scanner.nextLine();
        // required edges
        for (int i = 0; i < data.edges_r; i++)
            data.tasks[i + data.nodes_r] = extract(data, scanner, TaskType.EDGE, false);
        scanner.next();
        scanner.nextLine();
        // edges
        for (int i = 0; i < data.edges - data.edges_r; i++)
            extract(data, scanner, TaskType.EDGE, true);
        scanner.next();
        scanner.nextLine();
        // required arcs
        for (int i = 0; i < data.arcs_r; i++)
            data.tasks[i + data.nodes_r + data.edges_r] = extract(data, scanner, TaskType.ARC, false);
        scanner.next();
        scanner.nextLine();
        // arcs
        for (int i = 0; i < data.arcs - data.arcs_r; i++)
            extract(data, scanner, TaskType.ARC, true);
        scanner.close();
        return data;
    }

    private static int extract(Scanner scanner) {
        scanner.next();
        return scanner.nextInt();
    }

    private static int extract(Scanner scanner, int repeat) {
        for (int i = 0; i < repeat; i++)
            scanner.next();
        return scanner.nextInt();
    }

    private static Task extract(Data data, Scanner scanner, TaskType type, boolean deadhead) {
        String name = scanner.next();
        switch (type) {
            case NODE: {
                int node = Integer.parseInt(name.substring(1));
                int demand = scanner.nextInt();
                int cost = scanner.nextInt();
                NodeTask nt = new NodeTask(name, demand, cost, node);
                data.graph[node][node] = nt;
                return nt;
            }
            case EDGE: {
                int first = scanner.nextInt();
                int second = scanner.nextInt();
                int dist = scanner.nextInt();
                if (deadhead) {
                    data.raw_dist[first][second] = data.raw_dist[second][first] = dist;
                    data.graph[first][second] = new Deadhead(name, first, second, dist);
                    data.graph[second][first] = new Deadhead(name, second, first, dist);
                } else {
                    int demand = scanner.nextInt();
                    int cost = scanner.nextInt();
                    data.raw_dist[first][second] = data.raw_dist[second][first] = dist;
                    EdgeTask et = new EdgeTask(name, demand, cost, first, second, dist);
                    EdgeTask et2 = new EdgeTask(name, demand, cost, second, first, dist);
                    data.graph[first][second] = et;
                    data.graph[second][first] = et2;
                    data.edge_set.put(et, et2);
                    data.edge_set.put(et2, et);
                    return et;
                }
                break;
            }
            case ARC: {
                int head = scanner.nextInt();
                int tail = scanner.nextInt();
                int dist = scanner.nextInt();
                if (deadhead) {
                    data.raw_dist[head][tail] = dist;
                    data.graph[head][tail] = new Deadhead(name, head, tail, dist);
                } else {
                    int demand = scanner.nextInt();
                    int cost = scanner.nextInt();
                    data.raw_dist[head][tail] = dist;
                    ArcTask at = new ArcTask(name, demand, cost, head, tail, dist);
                    data.graph[head][tail] = at;
                    return at;
                }
                break;
            }
        }
        return null;
    }

}
