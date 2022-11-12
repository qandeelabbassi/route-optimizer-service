package tech.codeclever.util.repair;

import tech.codeclever.structure.*;
import tech.codeclever.util.MyParameter;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public abstract class Constructor {

    // todo: when doing construction, we need consider the constraints of vehicle number

    public Random random = MyParameter.random;

    public abstract void construct(List<Task> remains, Solution sol);

    public Set<Task> list2set(List<Task> list, Data data) {
        Set<Task> set = new HashSet<>(list);
        for (Task t : list) if (t.type == TaskType.EDGE) set.add(data.get_reverse_edge(t));
        return set;
    }

//    public int get_add_saving(Route r, int k, Task t) {
//        return t.dist - Data.dist[r.tasks.get(k - 1).to][r.tasks.get(k).from] + Data.dist[r.tasks.get(k - 1).to][t.from] + Data.dist[t.to][r.tasks.get(k).from];
//    }
}
