/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package movement;

import java.io.File;
import java.io.IOException;
import java.util.*;

import core.SettingsError;
import input.WKTReader;
import movement.helper.RandomHelper;
import movement.map.DijkstraPathFinder;
import movement.map.MapNode;
import movement.map.MapRoute;
import core.Coord;
import core.Settings;
import movement.map.SimMap;

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
    private String routeFileName;
    private int routeType;
    private DijkstraPathFinder pathFinder;
    private List<MapRoute> allRoutes = null;

    private Settings roomSettings;

    private List<MapNode> roomNodes;

    private RandomHelper randomHelper;

    /**
     * Creates a new movement model based on a Settings object's settings.
     * @param settings The Settings object where the settings are read from
     */
    public MapRoomMovement(Settings settings) {
        super(settings);
        RandomHelper.createInstance(rng);
        randomHelper = RandomHelper.getInstance();
        roomSettings = new Settings(MAP_ROOM_MOVEMENT_NS);
        String lrMapFileName = roomSettings.getSetting(LECTURE_ROOM_MAP_FILE_S);
        routeFileName = settings.getSetting(ROUTE_FILE_S);
        routeType = settings.getInt(ROUTE_TYPE_S);
        allRoutes = MapRoute.readRoutes(routeFileName, routeType, getMap());
        pathFinder = new DijkstraPathFinder(getOkMapNodeTypes());
        roomNodes = generatePointRoutes(allRoutes, routeFileName, lrMapFileName, getMap());
    }

    /**
     * Copyconstructor.
     * @param proto The MapRouteMovement prototype
     */
    protected MapRoomMovement(MapRoomMovement proto) {
        super(proto);

        this.randomHelper = proto.randomHelper;
        this.pathFinder = proto.pathFinder;
        this.allRoutes = proto.allRoutes;
        this.roomNodes = proto.roomNodes;
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

}
