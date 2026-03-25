package com.example.spacecolony;

public class Builder extends CrewMember{
    public Builder (String name) {
        super(name, 75, 5, 2, 8, 10);
    }
    public void upgrade(Building building) {
        building.increaseCapacity();
        building.increaseGain();
        if (building instanceof Simulator) {
            ((Simulator) building).decreaseEnergyCost();
        }
    }
}
