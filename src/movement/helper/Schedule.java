package movement.helper;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by netjinho on 29-11-2016.
 */
public class Schedule implements ScheduleInterface{
    private int[] startSchedule;
    private int[] endSchedule;

    public Schedule(){
        RandomHelper random = RandomHelper.getInstance();

        List<Integer> options = new ArrayList<>();
        List<Integer> start = new ArrayList<>();
        List<Integer> end = new ArrayList<>();

        options.add(8);
        options.add(10);
        options.add(12);
        options.add(14);
        options.add(16);
        options.add(18);

        int size = random.getRandomIntBetween(0,5); //Between no schedule and 4 classes a day

        for (int i = 0, idx; i<size; i++){
            idx = random.getRandomIntBetween(0,options.size());

            start.add(options.get(idx));
            end.add(options.get(idx) + 2);

            options.remove(idx);
        }

        Collections.sort(start);
        Collections.sort(end);

        startSchedule = start.stream().mapToInt(i -> i).toArray();
        endSchedule = end.stream().mapToInt(i -> i).toArray();
    }

    public int[] getStartTimesSorted(){
        return startSchedule;
    }


    public int[] getEndTimesSorted(){
        return endSchedule;
    }

    public boolean emptySchedule(int[] schedule){
        if (schedule.length == 0)
            return true;

        return false;
    }
}
