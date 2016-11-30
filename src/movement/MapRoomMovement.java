/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package movement;

import java.io.File;
import java.io.IOException;
import java.util.*;
import core.Coord;
import core.Settings;
import core.SettingsError;
import core.SimClock;
import input.WKTReader;
import movement.helper.RandomHelper;
import movement.helper.EnterExitHelper;
import movement.helper.Schedule;
import movement.map.DijkstraPathFinder;
import movement.map.MapNode;
import movement.map.MapRoute;
import movement.map.SimMap;
import java.util.ArrayList;
import java.util.List;


/**
 * Map based movement model that uses predetermined paths within the map area.
 * Predetermined paths are generated between point objects found in the map file.
 * Nodes using this model (can) stop on every route waypoint and find their
 * way to next waypoint using {@link DijkstraPathFinder}.
 */
public class MapRoomMovement extends MapBasedMovement {


    public static final String MAP_ROOM_MOVEMENT_NS = "MapRoomMovement";
    public static final String LECTURE_ROOM_MAP_FILE_S = "lectureRoomFile";
    public static final String ROUTE_FILE_S = "routeFile";
    public static final String ROUTE_TYPE_S = "routeType";
    public static final String CHANCE_FOR_UBAHN_SETTING = "chanceForUbahn";

    private String routeFileName;
    private int routeType;
    private DijkstraPathFinder pathFinder;
    private List<MapRoute> allRoutes = null;

    private Settings modelSettings;
    private Settings groupSettings;

    private List<MapNode> roomNodes;
    private RandomHelper randomHelper;
    private EnterExitHelper enterExitHelper;
    private double chanceForUbahn;

    private Schedule schedule;
    private int enterTime;
    private int exitTime;
    private boolean byUbahn;


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
        this.schedule = new Schedule(groupSettings, randomHelper);

        String lrMapFileName = modelSettings.getSetting(LECTURE_ROOM_MAP_FILE_S);
        routeFileName = settings.getSetting(ROUTE_FILE_S);
        routeType = settings.getInt(ROUTE_TYPE_S);
        allRoutes = MapRoute.readRoutes(routeFileName, routeType, getMap());
        pathFinder = new DijkstraPathFinder(getOkMapNodeTypes());
        roomNodes = generatePointRoutes(allRoutes, routeFileName, lrMapFileName, getMap());


        RandomHelper.createInstance(MovementModel.rng);
        this.randomHelper = RandomHelper.getInstance();

        this.chanceForUbahn = settings.getDouble(CHANCE_FOR_UBAHN_SETTING);
        this.byUbahn = this.randomHelper.getRandomDouble() < this.chanceForUbahn;

        this.enterExitHelper = new EnterExitHelper(settings);
        initEnterExitTime();
    }

    public MapRoomMovement(MapRoomMovement proto) {
        super(proto);
        this.groupSettings = proto.groupSettings;
        this.schedule = new Schedule(proto.groupSettings, proto.randomHelper);
        this.randomHelper = proto.randomHelper;
        this.enterExitHelper = proto.enterExitHelper;
        this.chanceForUbahn = proto.chanceForUbahn;
        this.pathFinder = proto.pathFinder;
        this.allRoutes = proto.allRoutes;
        this.roomNodes = proto.roomNodes;

        this.byUbahn = this.randomHelper.getRandomDouble() < this.chanceForUbahn;
        initEnterExitTime();
    }

    private void initEnterExitTime() {
        this.enterTime = this.enterExitHelper.enterTimeForSchedule(schedule, byUbahn);
        this.exitTime = this.enterExitHelper.exitTimeForSchedule(schedule, byUbahn, enterTime);
    }

    private List<MapNode> generatePointRoutes(List<MapRoute> tempRoutes, String fileName, String lrFileName, SimMap map) {

        WKTReader reader = new WKTReader();
        List<Coord> points;
        File routeFile;

        boolean mirror = map.isMirrored();
        double xOffset = map.getOffset().getX();
        double yOffset = map.getOffset().getY();

        try {
            routeFile = new File(lrFileName);
            points = reader.readPoints(routeFile);
        }
        catch (IOException ioe){
            throw new SettingsError("Couldn't read MapRoute-data file " +
                    fileName + 	" (cause: " + ioe.getMessage() + ")");
        }

        for (Coord point : points) {
            if (mirror) {
                point.setLocation(point.getX(), -point.getY());
            }
            point.translate(xOffset, yOffset);
        }

        Set<MapNode> roomNodes = new HashSet<>();
        for (MapRoute route : tempRoutes) {
            for (MapNode node : route.getStops()) {
                for (Coord point : points) {
                    if (node.getLocation().equals(point)) {
                        roomNodes.add(node);
                    }
                }
            }
        }

        return new ArrayList<>(roomNodes);
    }

    @Override
    public Path getPath() {
        Path p = new Path(generateSpeed());

        MapNode to = roomNodes.get(randomHelper.getRandomIntBetween(0, roomNodes.size()));

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
        if (lastMapNode == null) {
            lastMapNode = roomNodes.get(randomHelper.getRandomIntBetween(0, roomNodes.size()));
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
        return curTime;
    }
}
