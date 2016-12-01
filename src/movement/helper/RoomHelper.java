package movement.helper;

import core.Coord;
import core.Settings;
import core.SettingsError;
import input.WKTReader;
import movement.map.MapNode;
import movement.map.MapRoute;
import movement.map.SimMap;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Created by Hannes on 30.11.2016.
 */
public class RoomHelper {

    public static final String LECTURE_ROOM_PREFIX_S = "lr";
    public static final String LECTURE_ROOM_COUNT_S = "lrCount";
    public static final String ROUTE_FILE_S = "routeFile";
    public static final String ROOM_CAPACITY_OTHER_I = "roomCapacityOther";

    private Set<MapNode> roomNodes;
    private List<Room> rooms;
    private static RoomHelper instance = null;
    public Map<Room, Map<ScheduleSlot, Integer>> utilization;

    public RoomHelper(Settings modelSettings, List<MapRoute> routes, SimMap map) {
        this.readAllRooms(modelSettings, routes, map);
        this.utilization = new HashMap<>();
        for (Room room: this.rooms) {
            this.utilization.put(room, null);
        }
    }

    public static RoomHelper getInstance() {
        return RoomHelper.instance;
    }

    public static void createInstance(Settings modelSettings, List<MapRoute> routes, SimMap map) {
        if (RoomHelper.instance == null) {
            RoomHelper.instance = new RoomHelper(modelSettings, routes, map);
        }
    }

    public List<Room> getAllRooms() {
        return rooms;
    }

    public List<Room> getLectureRooms(){
        return rooms.stream().filter(x -> x.getType() == RoomType.LECTURE_ROOM).collect(Collectors.toList());
    }

    public List<Room> getOtherRooms() {
        return rooms.stream().filter(x -> x.getType() != RoomType.LECTURE_ROOM).collect(Collectors.toList());
    }

    private void readAllRooms(Settings settings, List<MapRoute> routes, SimMap map) {
        rooms = new ArrayList<>();
        roomNodes = getNodesForAllPointsInMap(settings, routes, map);
        rooms.addAll(readLectureRooms(settings, map));
        rooms.addAll(readOtherRooms(settings));
    }

    private static Set<MapNode> getNodesForAllPointsInMap(Settings settings, List<MapRoute> routes, SimMap map) {
        WKTReader reader = new WKTReader();
        String fileName = settings.getSetting(ROUTE_FILE_S);
        List<Coord> points;
        try {
            File routeFile = new File(fileName);
            points = reader.readPoints(routeFile);
        }
        catch (IOException ioe){
            throw new SettingsError("Couldn't read MapRoute-data file " +
                    fileName + 	" (cause: " + ioe.getMessage() + ")");
        }

        for (Coord point : points) {
            pointToMapCoord(point, map);
        }

        Set<MapNode> roomNodes = new HashSet<>();
        for (MapRoute route : routes) {
            for (MapNode node : route.getStops()) {
                for (Coord point : points) {
                    if (node.getLocation().equals(point)) {
                        roomNodes.add(node);
                    }
                }
            }
        }
        return roomNodes;
    }

    private List<Room> readLectureRooms(Settings settings, SimMap map) {

        List<Room> lectureRooms = new ArrayList<>();

        int count = settings.getInt(LECTURE_ROOM_COUNT_S);

        for (int i=1; i <= count; i++) {
            double[] raw = settings.getCsvDoubles(LECTURE_ROOM_PREFIX_S + i);
            Coord point = pointToMapCoord(raw[0], raw[1], map);
            for (MapNode node : roomNodes) {
                if (node.getLocation().equals(point)) {
                    lectureRooms.add(new Room(node, (int)raw[2], RoomType.LECTURE_ROOM));
                    roomNodes.remove(node);
                    break;
                }
            }
        }

        return lectureRooms;
    }

    private List<Room> readOtherRooms(Settings settings) {
        List<Room> otherRooms = new ArrayList<>();
        for (MapNode node : roomNodes) {
            otherRooms.add(new Room(node, settings.getInt(ROOM_CAPACITY_OTHER_I), RoomType.OTHER));
        }
        return otherRooms;
    }

    private static Coord pointToMapCoord(Coord point, SimMap map) {

        boolean mirror = map.isMirrored();
        double xOffset = map.getOffset().getX();
        double yOffset = map.getOffset().getY();

        if (mirror) {
            point.setLocation(point.getX(), -point.getY());
        }
        point.translate(xOffset, yOffset);

        return point;
    }

    private static Coord pointToMapCoord(double x, double y, SimMap map) {
       return pointToMapCoord(new Coord(x,y), map);
    }
}
