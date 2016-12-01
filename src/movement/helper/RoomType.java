package movement.helper;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Hannes on 30.11.2016.
 */
public enum RoomType {
    LECTURE_ROOM(1),
    OTHER(2),
    ENTRY_EXIT(3);

    private int roomNr;

    private static Map<Integer, RoomType> map = new HashMap<Integer, RoomType>();

    static {
        for (RoomType legEnum : RoomType.values()) {
            map.put(legEnum.roomNr, legEnum);
        }
    }

    RoomType(final int roomNr) {
        this.roomNr = roomNr;
    }

    public static RoomType valueOf(int roomNr) {
        return map.get(roomNr);
    }
}