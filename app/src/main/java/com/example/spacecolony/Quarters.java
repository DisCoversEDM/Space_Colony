package com.example.spacecolony;

import java.util.ArrayList;

public class Quarters {
    public static Quarters instance;
    private Quarters () {}
    public static Quarters getInstance () {
        if (instance == null) {
            instance = new Quarters();
        }
        return instance;
    }

    public static void reset() {
        instance = null;
    }

    public void rest (ArrayList<CrewMember> crew) {
        for (CrewMember cm : crew) {
            cm.changeEnergy(1);
        }
    }
}
