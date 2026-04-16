package com.example.spacecolony;

public abstract class Building {
    protected int capacity;
    protected int gain;
    private int upgradeCost;
    private Builder assignedBuilder;
    private boolean upgradePending;

    public Building () {
        this.capacity = 1;
        this.gain = 2;
        this.upgradeCost = 10;
        this.upgradePending = false;
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
    public int getCapacity() {
        return this.capacity;
    }

    public boolean isUpgradePending() {
        return upgradePending;
    }

    public void setUpgradePending(boolean upgradePending, Builder builder) {
        this.upgradePending = upgradePending;
        this.assignedBuilder = builder;
    }

    public Builder getAssignedBuilder() {
        return assignedBuilder;
    }

    public void completeUpgrade() {
        if (upgradePending && assignedBuilder != null) {
            // Note: assignedBuilder.upgrade(this) already handles capacity and gain increases
            assignedBuilder.upgrade(this);
            increaseUpgradeCost();
            upgradePending = false;
            assignedBuilder = null;
        }
    }
}
