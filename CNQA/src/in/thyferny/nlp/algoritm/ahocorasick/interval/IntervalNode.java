package in.thyferny.nlp.algoritm.ahocorasick.interval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class IntervalNode
{
    
    private enum Direction
    {
        LEFT, RIGHT
    }

    
    private IntervalNode left = null;
    
    private IntervalNode right = null;
    
    private int point;
    
    private List<Intervalable> intervals = new ArrayList<Intervalable>();

    
    public IntervalNode(List<Intervalable> intervals)
    {
        this.point = determineMedian(intervals);

        List<Intervalable> toLeft = new ArrayList<Intervalable>();  // 以中点为界靠左的区间
        List<Intervalable> toRight = new ArrayList<Intervalable>(); // 靠右的区间

        for (Intervalable interval : intervals)
        {
            if (interval.getEnd() < this.point)
            {
                toLeft.add(interval);
            }
            else if (interval.getStart() > this.point)
            {
                toRight.add(interval);
            }
            else
            {
                this.intervals.add(interval);
            }
        }

        if (toLeft.size() > 0)
        {
            this.left = new IntervalNode(toLeft);
        }
        if (toRight.size() > 0)
        {
            this.right = new IntervalNode(toRight);
        }
    }

    
    public int determineMedian(List<Intervalable> intervals)
    {
        int start = -1;
        int end = -1;
        for (Intervalable interval : intervals)
        {
            int currentStart = interval.getStart();
            int currentEnd = interval.getEnd();
            if (start == -1 || currentStart < start)
            {
                start = currentStart;
            }
            if (end == -1 || currentEnd > end)
            {
                end = currentEnd;
            }
        }
        return (start + end) / 2;
    }

    
    public List<Intervalable> findOverlaps(Intervalable interval)
    {

        List<Intervalable> overlaps = new ArrayList<Intervalable>();

        if (this.point < interval.getStart())
        {
            // 右边找找
            addToOverlaps(interval, overlaps, findOverlappingRanges(this.right, interval));
            addToOverlaps(interval, overlaps, checkForOverlapsToTheRight(interval));
        }
        else if (this.point > interval.getEnd())
        {
            // 左边找找
            addToOverlaps(interval, overlaps, findOverlappingRanges(this.left, interval));
            addToOverlaps(interval, overlaps, checkForOverlapsToTheLeft(interval));
        }
        else
        {
            // 否则在当前区间
            addToOverlaps(interval, overlaps, this.intervals);
            addToOverlaps(interval, overlaps, findOverlappingRanges(this.left, interval));
            addToOverlaps(interval, overlaps, findOverlappingRanges(this.right, interval));
        }

        return overlaps;
    }

    
    protected void addToOverlaps(Intervalable interval, List<Intervalable> overlaps, List<Intervalable> newOverlaps)
    {
        for (Intervalable currentInterval : newOverlaps)
        {
            if (!currentInterval.equals(interval))
            {
                overlaps.add(currentInterval);
            }
        }
    }

    
    protected List<Intervalable> checkForOverlapsToTheLeft(Intervalable interval)
    {
        return checkForOverlaps(interval, Direction.LEFT);
    }

    
    protected List<Intervalable> checkForOverlapsToTheRight(Intervalable interval)
    {
        return checkForOverlaps(interval, Direction.RIGHT);
    }

    
    protected List<Intervalable> checkForOverlaps(Intervalable interval, Direction direction)
    {

        List<Intervalable> overlaps = new ArrayList<Intervalable>();
        for (Intervalable currentInterval : this.intervals)
        {
            switch (direction)
            {
                case LEFT:
                    if (currentInterval.getStart() <= interval.getEnd())
                    {
                        overlaps.add(currentInterval);
                    }
                    break;
                case RIGHT:
                    if (currentInterval.getEnd() >= interval.getStart())
                    {
                        overlaps.add(currentInterval);
                    }
                    break;
            }
        }
        return overlaps;
    }

    
    protected static List<Intervalable> findOverlappingRanges(IntervalNode node, Intervalable interval)
    {
        if (node != null)
        {
            return node.findOverlaps(interval);
        }
        return Collections.emptyList();
    }

}
