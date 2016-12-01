package movement.helper;

import movement.map.MapNode;

/**
 * Created by Hannes on 30.11.2016.
 */
public class Room {
    private int capacity;
    private MapNode node;
    private RoomType type;

    public Room(MapNode node, int capacity, RoomType type) {
        this.node = node;
        this.capacity = capacity;
        this.type = type;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public MapNode getNode() {
        return node;
    }

    public void setNode(MapNode node) {
        this.node = node;
    }

    public RoomType getType() {
        return type;
    }

    public void setType(RoomType type) {
        this.type = type;
    }
}
