package movement.helper;

import core.Settings;
import util.Tuple;

import java.util.*;

/**
 * Created by netjinho on 29-11-2016.
 */
public class Schedule implements ScheduleInterface{

    public static final String SCHEDULE_TIME_SETTING = "scheduleTime";
    public static final String AVERAGE_COURSES_PER_DAY_SETTING = "averageCoursesPerDaySetting";
    public static final String FIXED_SCHEDULE_FILE_SETTING = "fixedScheduleFile";

    private List<Tuple<Integer, Integer>> timeSlots;

    public Schedule(Settings settings, RandomHelper random){

        timeSlots = new ArrayList<>();

        double coursesPerDay = settings.getInt(AVERAGE_COURSES_PER_DAY_SETTING);
        int[] times = settings.getCsvInts(SCHEDULE_TIME_SETTING);

        List<Integer> startTimes = new ArrayList<>(times.length);

        for (int time: times) {
            startTimes.add(time);
        }

        List<Integer> endTimes = new ArrayList<>(startTimes);
        startTimes.remove(startTimes.size()-1);
        endTimes.remove(0);

        for (int i = 0; i < coursesPerDay; i++){
            int randomSlot = random.getRandomIntBetween(0, startTimes.size());
            int startTime = startTimes.get(randomSlot);
            int endTime = endTimes.get(randomSlot);

            timeSlots.add(new Tuple<>(startTime, endTime));
            startTimes.remove(randomSlot);
            endTimes.remove(randomSlot);
        }

        Collections.sort(timeSlots, Comparator.comparing(Tuple::getKey));
    }

    public int[] getStartTimesSorted(){
        return timeSlots.stream().mapToInt(i -> i.getKey()).toArray();
    }


    public int[] getEndTimesSorted(){
        return timeSlots.stream().mapToInt(i -> i.getValue()).toArray();
    }

    public boolean emptySchedule(int[] schedule){
        if (schedule.length == 0)
            return true;

        return false;
    }
}
