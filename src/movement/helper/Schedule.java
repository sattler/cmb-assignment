package movement.helper;

import core.Settings;
import core.SettingsError;

import java.io.*;
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
        parseScheduleFile();
        //ScheduleSlot newSlot = new ScheduleSlot(12000,24000,null);
        ///setRandomRoomForSlot(newSlot,roomCapacities,random);
        //timeSlots.add(newSlot);
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

    public void parseScheduleFile() {
        List<String> schedules = new ArrayList<>();
        try
        {
            File file = new File(FIXED_SCHEDULE_FILE_SETTING);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null)
            {
                schedules.add(line);
            }
            reader.close();
        }
        catch (IOException ioe) {
            throw new SettingsError("Couldn't read schedule file " +
                    FIXED_SCHEDULE_FILE_SETTING + " (cause: " + ioe.getMessage() + ")");
        }

        List<ScheduleSlot> slots = new ArrayList<>();

        //String[] first = schedules.get(0).split(",");
        //String[] second = schedules.get(1).split(",");

        String[] aux = null;
        String[] aux2 = null;
        ScheduleSlot newSlot = null;

        for(int i = 0; i < schedules.size(); i++) {
            aux2 = schedules.get(i).split(",");
            for (String token : aux2) {
                aux = token.split("-");
                newSlot = new ScheduleSlot(Integer.parseInt(aux[0]), Integer.parseInt(aux[1]), null);
                slots.add(newSlot);
            }
        }
        /*for(String token : second){
            aux = token.split("-");
            newSlot = new ScheduleSlot(aux[0], aux[1], null);
            slots.add(newSlot);
        }*/

        //TODO Assign fixed rooms
    }
}
