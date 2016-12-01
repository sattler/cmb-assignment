/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package movement;

import core.Coord;
import core.Settings;
import core.SimClock;
import movement.helper.*;
import movement.map.DijkstraPathFinder;
import movement.map.MapNode;
import movement.map.MapRoute;

import java.util.List;


/**
 * Map based movement model that uses predetermined paths within the map area.
 * Predetermined paths are generated between point objects found in the map file.
 * Nodes using this model (can) stop on every route waypoint and find their
 * way to next waypoint using {@link DijkstraPathFinder}.
 */
public class MapRoomMovement extends MapBasedMovement {


    public static final String MAP_ROOM_MOVEMENT_NS = "MapRoomMovement";
    public static final String ROUTE_FILE_S = "routeFile";
    public static final String ROUTE_TYPE_S = "routeType";
    public static final String CHANCE_FOR_UBAHN_SETTING = "chanceForUbahn";
    public static final String START_PEAK_ENTER_TIME_DIFFERENCE_SETTING = "startPeakEnterTimeDifference";
    public static final String ENTER_LECTURE_STDDEV_SETTING = "enterLectureStddev";

    private String routeFileName;
    private int routeType;
    private DijkstraPathFinder pathFinder;
    private List<MapRoute> allRoutes = null;

    private Settings modelSettings;
    private Settings groupSettings;

    private List<Room> rooms;
    private RandomHelper randomHelper;
    private RoomHelper roomHelper;
    private EnterExitHelper enterExitHelper;
    private double chanceForUbahn;

    private Schedule schedule;
    private int enterTime;
    private int exitTime;
    private boolean byUbahn;

    private final int StartPeakEnterTimeDifference;
    private final double EnterLectureStddev;


    /**
     * Creates a new movement model based on a Settings object's settings.
     * @param settings The Settings object where the settings are read from
     */
    public MapRoomMovement(Settings settings) {
        super(settings);
        RandomHelper.createInstance(rng);
        randomHelper = RandomHelper.getInstance();

        this.modelSettings = new Settings(MAP_ROOM_MOVEMENT_NS);
        this.groupSettings = settings;

        EnterLectureStddev = settings.getDouble(ENTER_LECTURE_STDDEV_SETTING);
        StartPeakEnterTimeDifference = settings.getInt(START_PEAK_ENTER_TIME_DIFFERENCE_SETTING);

        routeFileName = modelSettings.getSetting(ROUTE_FILE_S);
        routeType = modelSettings.getInt(ROUTE_TYPE_S);
        allRoutes = MapRoute.readRoutes(routeFileName, routeType, getMap());
        RoomHelper.createInstance(modelSettings, allRoutes, getMap());
        roomHelper = RoomHelper.getInstance();
        pathFinder = new DijkstraPathFinder(getOkMapNodeTypes());

        // @TODO: @Patrick here query RoomHelper for rooms according to user group
        rooms = roomHelper.getAllRooms();
        this.schedule = new Schedule(groupSettings, randomHelper, roomHelper.utilization);
        //rooms = roomHelper.getRoomsWithType(RoomType.ENTRY_EXIT);
        //rooms = roomHelper.getRoomsWithType(RoomType.OTHER);

        RandomHelper.createInstance(MovementModel.rng);
        this.randomHelper = RandomHelper.getInstance();

        this.chanceForUbahn = groupSettings.getDouble(CHANCE_FOR_UBAHN_SETTING);
        this.byUbahn = this.randomHelper.getRandomDouble() < this.chanceForUbahn;

        this.enterExitHelper = new EnterExitHelper(settings);
        initEnterExitTime();
    }

    public MapRoomMovement(MapRoomMovement proto) {
        super(proto);
        this.groupSettings = proto.groupSettings;
        this.modelSettings = proto.modelSettings;
        this.schedule = new Schedule(proto.groupSettings, proto.randomHelper, proto.roomHelper.utilization);
        this.randomHelper = proto.randomHelper;
        this.enterExitHelper = proto.enterExitHelper;
        this.chanceForUbahn = proto.chanceForUbahn;
        this.pathFinder = proto.pathFinder;
        this.allRoutes = proto.allRoutes;
        this.rooms = proto.rooms;
        this.roomHelper = proto.roomHelper;

        this.EnterLectureStddev = proto.EnterLectureStddev;
        this.StartPeakEnterTimeDifference = proto.StartPeakEnterTimeDifference;

        this.byUbahn = this.randomHelper.getRandomDouble() < this.chanceForUbahn;
        this.initEnterExitTime();
    }

    private void initEnterExitTime() {
        Integer enterTime = this.enterExitHelper.enterTimeForSchedule(schedule, byUbahn);
        this.enterTime = enterTime != null ? enterTime : Integer.MAX_VALUE;
        if (enterTime == null) {
            this.exitTime = this.enterTime;
        } else {
            this.exitTime = this.enterExitHelper.exitTimeForSchedule(schedule, byUbahn, enterTime);
        }
    }

    @Override
    public Path getPath() {
        Path p = new Path(generateSpeed());

        MapNode to;
        final int curTime = (int)SimClock.getTime();
        final int timeInsecurity = 10;
        ScheduleSlot nextSlot = this.schedule.getNextScheduleSlot(curTime - timeInsecurity);
        ScheduleSlot activeSlot = this.schedule.getActiveScheduleSlot(curTime);
        final int fiveMinutes = 300;
        if (nextSlot != null && nextSlot.getStartTime() - curTime < fiveMinutes) {
            to = nextSlot.getRoom().getNode();
        } else if (activeSlot != null) {
            to = activeSlot.getRoom().getNode();
        } else {
            to = this.roomHelper.getOtherRooms().get(this.randomHelper.getRandomIntBetween(0,
                    this.roomHelper.getOtherRooms().size())).getNode();
        }

        if (this.lastMapNode == to) {
            return null;
        }

        List<MapNode> nodePath = pathFinder.getShortestPath(lastMapNode, to);

        // this assertion should never fire if the map is checked in read phase
        assert nodePath.size() > 0 : "No path from " + lastMapNode + " to " +
                to + ". The simulation map isn't fully connected";

        for (MapNode node : nodePath) { // create a Path from the shortest path
            p.addWaypoint(node.getLocation());
        }

        lastMapNode = to;

        return p;
    }

    /**
     * Returns the first stop on the route which is one of the lecture rooms
     */
    @Override
    public Coord getInitialLocation() {
        if (nextPathAvailable() > 100000) {
            return null;
        }
        if (lastMapNode == null) {
            lastMapNode = this.roomHelper.getOtherRooms().get(
                    this.randomHelper.getRandomIntBetween(0, this.roomHelper.getOtherRooms().size())).getNode();
        }

        return lastMapNode.getLocation().clone();
    }

    @Override
    public Coord getLastLocation() {
        if (lastMapNode != null) {
            return lastMapNode.getLocation().clone();
        } else {
            return null;
        }
    }
    
    @Override
    public MapRoomMovement replicate() {
        return new MapRoomMovement(this);
    }

    @Override
    public boolean isActive() {
        return SimClock.getIntTime() >= this.enterTime && SimClock.getIntTime() <= this.exitTime;
    }

    @Override
    public double nextPathAvailable() {
        final double curTime = SimClock.getTime();
        if ( curTime < this.enterTime ) {
            return this.enterTime;
        } else if ( curTime > this.exitTime ) {
            return Double.MAX_VALUE;
        }
        ScheduleSlot activeSlot = this.schedule.getActiveScheduleSlot((int)curTime);
        if (activeSlot != null) {
            return activeSlot.getEndTime();
        }
        ScheduleSlot nextSlot = this.schedule.getNextScheduleSlot((int)curTime);
        if (nextSlot != null) {
            return this.randomHelper.getNormalRandomWithMeanAndStddev(
                    nextSlot.getStartTime() - this.StartPeakEnterTimeDifference, this.EnterLectureStddev);
        }
        return this.exitTime;
    }
}
