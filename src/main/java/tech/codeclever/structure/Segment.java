package tech.codeclever.structure;

import org.apache.commons.lang3.ArrayUtils;
import tech.codeclever.util.MyParameter;

import java.util.Arrays;
import java.util.List;

public class Segment {
    /* shortest path from A to B*/
    public int start;
    public int end;
    public int[] list;
    public final Data data;

    public Segment(List<Integer> path, Data data) {
        this.data = data;
        list = path.stream().mapToInt(Integer::intValue).toArray();
        start = list[0];
        end = list[list.length - 1];
    }

    public Segment(int[] path, Data data) {
        this.data = data;
        list = path;
        start = list[0];
        end = list[list.length - 1];
    }

    /**
     * 连接两个Segment
     *
     * @param segment 需要被连接的Segment
     * @return 结果
     */
    public Segment connect(Segment segment) {
        int[] l;
        // 尾首相同
        if (end == segment.start) {
            l = ArrayUtils.addAll(this.list, Arrays.copyOfRange(segment.list, 1, segment.list.length));
        }
        // 尾首可直接相连
        else if (data.raw_dist[end][segment.start] < MyParameter.BIG_NUM) {
            l = ArrayUtils.addAll(this.list, segment.list);
        }
        // 尾首不可以直接相连
        else {
            Segment s = data.segments[end][segment.start];
            return connect(s).connect(segment);
        }
        return new Segment(l, this.data);
    }

    public Segment connect(Task task) {
        if (task.type == TaskType.NODE) {
            return connect(new Segment(new int[]{task.from}, this.data));
        } else {
            return connect(new Segment(new int[]{task.from, task.to}, this.data));
        }
    }

    @Override
    public String toString() {
        return "Segment{" + Arrays.toString(list) + '}';
    }
}
