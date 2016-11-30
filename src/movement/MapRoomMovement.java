/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package movement;

import core.Coord;
import core.Settings;
import core.SettingsError;
import core.SimClock;
import input.WKTReader;
import movement.helper.EnterExitHelper;
import movement.helper.RandomHelper;
import movement.helper.Schedule;
import movement.map.DijkstraPathFinder;
import movement.map.MapNode;
import movement.map.MapRoute;
import movement.map.SimMap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static movement.map.MapRoute.CIRCULAR;
import static movement.map.MapRoute.PINGPONG;

/**
 * Map based movement model that uses predetermined paths within the map area.
 * Predetermined paths are generated between point objects found in the map file.
 * Nodes using this model (can) stop on every route waypoint and find their
 * way to next waypoint using {@link DijkstraPathFinder}. There can be
 * different type of routes; see {@link #ROUTE_TYPE_S}.
 */
public class MapRoomMovement extends MapRouteMovement {

    public static final String CHANCE_FOR_UBAHN_SETTING = "ChanceForUbahn";

    private RandomHelper randomHelper;
    private EnterExitHelper enterExitHelper;
    private double chanceForUbahn;

    private Schedule schedule;
    private Settings settings;
    private int enterTime;
    private int exitTime;
    private boolean byUbahn;


    /**
     * Creates a new movement model based on a Settings object's settings.
     * @param settings The Settings object where the settings are read from
     */
    public MapRoomMovement(Settings settings) {
        super(settings);
        allRoutes = generatePointRoutes(allRoutes, routeFileName, routeType, getMap());
        initFirstIndex(settings);

        this.settings = settings;
        this.schedule = new Schedule(settings);

        RandomHelper.createInstance(MovementModel.rng);
        this.randomHelper = RandomHelper.getInstance();

        this.chanceForUbahn = settings.getDouble(CHANCE_FOR_UBAHN_SETTING);
        this.byUbahn = this.randomHelper.getRandomDouble() < this.chanceForUbahn;

        this.enterExitHelper = new EnterExitHelper(settings);
        initEnterExitTime();
    }

    public MapRoomMovement(MapRoomMovement proto) {
        super(proto);
        this.settings = proto.settings;
        this.schedule = new Schedule(proto.settings);
        this.randomHelper = proto.randomHelper;
        this.enterExitHelper = proto.enterExitHelper;
        this.chanceForUbahn = proto.chanceForUbahn;

        this.byUbahn = this.randomHelper.getRandomDouble() < this.chanceForUbahn;
        initEnterExitTime();
    }

    private void initEnterExitTime() {
        this.enterTime = this.enterExitHelper.enterTimeForSchedule(schedule, byUbahn);
        this.exitTime = this.enterExitHelper.exitTimeForSchedule(schedule, byUbahn, enterTime);
    }

    private List<MapRoute> generatePointRoutes(List<MapRoute> tempRoutes, String fileName, int type, SimMap map) {

        List<MapRoute> routes = new ArrayList<>();
        WKTReader reader = new WKTReader();
        List<Coord> points;
        File routeFile;

        boolean mirror = map.isMirrored();
        double xOffset = map.getOffset().getX();
        double yOffset = map.getOffset().getY();

        if (type != CIRCULAR && type != PINGPONG) {
            throw new SettingsError("Invalid route type (" + type + ")");
        }

        try {
            routeFile = new File(fileName);
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

        HashMap<Coord, MapNode> roomMapNode = new HashMap<>();
        for (MapRoute route : tempRoutes) {
            for (MapNode node : route.getStops()) {
                for (Coord point : points) {
                    if (node.getLocation().equals(point)) {
                        roomMapNode.put(point, node);
                    }
                }
            }
        }

        for (MapNode nOutside : roomMapNode.values()){
            for (MapNode nInside : roomMapNode.values()){
                if (!nOutside.equals(nInside)) {
                    routes.add(new MapRoute(type, pathFinder.getShortestPath(nOutside, nInside)));
                }
            }
        }
        return routes;
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
