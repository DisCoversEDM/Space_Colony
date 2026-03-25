package com.example.spacecolony;

import java.util.ArrayList;

public class Hospital extends Building {
    public static Hospital instance;
    private Hospital () {
        super();
    }
    public static Hospital getInstance () {
        if (instance == null) {
            instance = new Hospital();
        }
        return instance;
    }
    public void heal (Medic medic, ArrayList<CrewMember> crew) {
        if (crew.size() <= this.capacity) {
            for (CrewMember cm : crew) {
                cm.changeEnergy(1);
                cm.heal(this.gain);
            }
        }
    }
}
