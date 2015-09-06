package in.thyferny.nlp.algoritm.ahocorasick.interval;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


public class IntervalTree
{
    
    private IntervalNode rootNode = null;

    
    public IntervalTree(List<Intervalable> intervals)
    {
        this.rootNode = new IntervalNode(intervals);
    }

    
    public List<Intervalable> removeOverlaps(List<Intervalable> intervals)
    {

        // 排序，按照先大小后左端点的顺序
        Collections.sort(intervals, new IntervalableComparatorBySize());

        Set<Intervalable> removeIntervals = new TreeSet<Intervalable>();

        for (Intervalable interval : intervals)
        {
            // 如果区间已经被移除了，就忽略它
            if (removeIntervals.contains(interval))
            {
                continue;
            }

            // 否则就移除它
            removeIntervals.addAll(findOverlaps(interval));
        }

        // 移除所有的重叠区间
        for (Intervalable removeInterval : removeIntervals)
        {
            intervals.remove(removeInterval);
        }

        // 排序，按照左端顺序
        Collections.sort(intervals, new IntervalableComparatorByPosition());

        return intervals;
    }

    
    public List<Intervalable> findOverlaps(Intervalable interval)
    {
        return rootNode.findOverlaps(interval);
    }

}
