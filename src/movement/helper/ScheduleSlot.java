package movement.helper;

/**
 * Created by Sattler Patrick on 01/12/16.
 */
public class ScheduleSlot {

    private int startTime;
    private int endTime;
    private Room room;

    ScheduleSlot(int startTime, int endTime, Room room) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
    }

    ScheduleSlot(ScheduleSlot proto) {
        this.startTime = proto.startTime;
        this.endTime = proto.endTime;
        this.room = null;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ScheduleSlot) {
            ScheduleSlot scheduleSlot = (ScheduleSlot) obj;
            return this.endTime == scheduleSlot.endTime && this.startTime == scheduleSlot.startTime;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.startTime * 100 + this.endTime;
    }
}
