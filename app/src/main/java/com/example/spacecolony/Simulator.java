package com.example.spacecolony;

import java.util.ArrayList;

public class Simulator extends Building{
    private int energyCost;
    public static Simulator instance;
    private Simulator () {
        super();
        this.energyCost = 3;
    }
    public static Simulator getInstance () {
        if (instance == null) {
            instance = new Simulator();
        }
        return instance;
    }
    public void decreaseEnergyCost () {
        if (this.energyCost > 1) {
            this.energyCost--;
        }
    }
    public void train (ArrayList<CrewMember> crew) {
        if (crew.size() <= this.capacity) {
            for (CrewMember cm : crew) {
                cm.changeEnergy(-this.energyCost);
                cm.increaseXP(this.gain);
            }
        }
    }
}
