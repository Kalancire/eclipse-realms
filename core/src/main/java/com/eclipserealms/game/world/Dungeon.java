package com.eclipserealms.game.world;

/** A dungeon is a sequence of procedurally generated floors. Floors are
 *  generated lazily (one at a time) rather than all up-front, which keeps
 *  memory use flat regardless of how deep the dungeon goes - important on
 *  8GB-RAM phones where the OS is already using a large chunk of that. */
public class Dungeon {

    public final String dungeonName;
    public final int totalFloors;
    private final long baseSeed;
    private DungeonFloor currentFloor;
    private int currentFloorIndex = 1;

    public Dungeon(String dungeonName, int totalFloors, long baseSeed) {
        this.dungeonName = dungeonName;
        this.totalFloors = totalFloors;
        this.baseSeed = baseSeed;
        this.currentFloor = new DungeonFloor(1, baseSeed);
    }

    public DungeonFloor getCurrentFloor() {
        return currentFloor;
    }

    public boolean hasNextFloor() {
        return currentFloorIndex < totalFloors;
    }

    public DungeonFloor descend() {
        if (!hasNextFloor()) return currentFloor;
        currentFloorIndex++;
        currentFloor = new DungeonFloor(currentFloorIndex, baseSeed + currentFloorIndex * 7919L);
        return currentFloor;
    }

    public int getCurrentFloorIndex() {
        return currentFloorIndex;
    }
}
