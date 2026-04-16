package com.example.spacecolony;

public class EnergyRestore extends Item {
    private int energyRestore;
    public EnergyRestore () {
        super(10);
        this.energyRestore = 4;
    }
    public void used (CrewMember cm) {
        cm.changeEnergy(this.energyRestore);
    }
    public int getEnergyAmount() {
        return energyRestore;
    }
}
