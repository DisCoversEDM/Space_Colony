package com.example.spacecolony;

public abstract class Building {
    protected int capacity;
    protected int gain;
    private int upgradeCost;
    public Building () {
        this.capacity = 1;
        this.gain = 2;
        this.upgradeCost = 10;
    }
    public void increaseCapacity () {
        this.capacity++;
    }
    public void increaseGain () {
        this.gain++;
    }
    public void increaseUpgradeCost () {
        this.upgradeCost += 5;
    }
    public int getUpgradeCost () {
        return this.upgradeCost;
    }
}
