package tech.codeclever.util.destroy;

import tech.codeclever.structure.*;

import java.util.*;

public class Tree_Destructor implements Destructor {

    //  This is a new operator that is particularly effective for MCGRP instances
    //  that contain all three task types.
    //  It utilizes the instance graph, as illustrated in Figure 1.
    //  First, it randomly selects a task as a root node,
    //  and then grows a tree in the instance graph G, from this root,
    //  by using a breadth-first strategy.(BFS) => use Queue, visited[]
    //  The growth is halted as soon as k tasks (of any kind) are encountered.

    @Override
    public List<Task> destruct(Data data, int k, Solution sol) {

        List<Task> removed = new ArrayList<>();
        HashSet<Task> set = bfs_tree_k_task(data, k, sol);

        for (Route r : sol.routes) {
            int i = 1;
            if (set.isEmpty())
                break;
            while (i < r.tasks.size() - 1) {
                Task t = r.tasks.get(i);
                if (set.contains(t)) {
                    removed.add(r.remove(i));
                    continue;
                }
                i++;
            }
        }
        sol.emptyRouteCheck();

        return removed;
    }

    private HashSet<Task> bfs_tree_k_task(Data data, int k, Solution s) {
        HashSet<Task> set = new HashSet<>();
        Queue<Task> queue = new LinkedList<>();
        // 1. select a task randomly
        int _r = random.nextInt(s.routes.size());
        int _t = random.nextInt(s.routes.get(_r).tasks.size() - 2) + 1;
        Task task = s.routes.get(_r).tasks.get(_t);
        // 2. bfs
        queue.offer(task);
        while (set.size() < k && !queue.isEmpty()) {
            // get the task from queue
            Task t = queue.poll();
            // next task connected to current task t, add them into queue
            for (int i = 1; i <= data.nodes; i++) {
                if (data.graph[t.to][i] != null &&
                        (data.graph[t.to][i].type == TaskType.DEAD
                                || !set.contains(data.graph[t.to][i]))) {
                    queue.offer(data.graph[t.to][i]);
                }
            }
            // add task t into set
            if (t.type != TaskType.DEAD)
                if (!(set.contains(t) || set.contains(data.get_reverse_edge(t)))) set.add(t);
        }
        return set;
    }
}
