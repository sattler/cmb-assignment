package movement.helper;

import core.Settings;

import java.util.*;

/**
 * Created by netjinho on 29-11-2016.
 */
public class Schedule implements ScheduleInterface{

    public static final String SCHEDULE_TIME_SETTING = "scheduleTime";
    public static final String AVERAGE_COURSES_PER_DAY_SETTING = "averageCoursesPerDaySetting";
    public static final String FIXED_SCHEDULE_FILE_SETTING = "fixedScheduleFile";

    private List<ScheduleSlot> timeSlots;

    public Schedule(Settings settings, RandomHelper random, Map<Room, Map<ScheduleSlot, Integer>> roomCapacities) {

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

            ScheduleSlot newSlot = new ScheduleSlot(startTime, endTime, null);

            setRandomRoomForSlot(newSlot, roomCapacities, random);

            timeSlots.add(newSlot);
            startTimes.remove(randomSlot);
            endTimes.remove(randomSlot);
        }

        timeSlots.sort(Comparator.comparing(ScheduleSlot::getStartTime));
    }

    private void setRandomRoomForSlot(ScheduleSlot slot, Map<Room, Map<ScheduleSlot, Integer>> roomCapacities,
                                         RandomHelper random) {
        Room retRoom = null;
        while (retRoom == null) {
            int roomIndex = random.getRandomIntBetween(0, roomCapacities.size());
            Room room = (Room) roomCapacities.keySet().toArray()[roomIndex];
            Map<ScheduleSlot, Integer> roomSchedule = roomCapacities.get(room);
            if (roomSchedule == null) {
                Map<ScheduleSlot, Integer> newRoomSchedule = new HashMap<>();
                newRoomSchedule.put(new ScheduleSlot(slot), 1);
                roomCapacities.put(room, newRoomSchedule);
                retRoom = room;
            } else {
                Integer slotCap = roomSchedule.get(slot);
                if (slotCap == null) {
                    roomSchedule.put(new ScheduleSlot(slot), 1);
                    retRoom = room;
                } else if (slotCap < room.getCapacity()) {
                    roomSchedule.put(new ScheduleSlot(slot), slotCap + 1);
                    retRoom = room;
                }
            }
        }
        slot.setRoom(retRoom);
    }

    public int[] getStartTimesSorted(){
        return timeSlots.stream().mapToInt(ScheduleSlot::getStartTime).toArray();
    }


    public int[] getEndTimesSorted(){
        return timeSlots.stream().mapToInt(ScheduleSlot::getEndTime).toArray();
    }

    public ScheduleSlot getNextScheduleSlot(int curTime) {
        int curDif = -1;
        ScheduleSlot nextSlot = null;
        for (ScheduleSlot slot: timeSlots) {
            int timeDif = slot.getStartTime() - curTime;
            if (timeDif > 0 && (curDif > timeDif || nextSlot == null)) {
                curDif = timeDif;
                nextSlot = slot;
            }
        }
        return nextSlot;
    }

    public ScheduleSlot getActiveScheduleSlot(int curTime) {
        for (ScheduleSlot slot: timeSlots) {
            if (slot.getStartTime() < curTime && slot.getEndTime() > curTime) {
                return slot;
            }
        }
        return null;
    }
}
