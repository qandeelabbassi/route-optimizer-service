package tech.codeclever.util.destroy;

import tech.codeclever.structure.Data;
import tech.codeclever.structure.Solution;
import tech.codeclever.structure.Task;
import tech.codeclever.util.MyParameter;

import java.util.List;
import java.util.Random;

public interface Destructor {

    Random random = MyParameter.random;

    List<Task> destruct(Data data, int k, Solution sol);
}
