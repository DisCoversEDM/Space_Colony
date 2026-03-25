package com.example.spacecolony;

public class FirstAidKit extends Item {
    private int hpRestore;
    public FirstAidKit () {
        super(30);
        this.hpRestore = 10;
    }
    public void used (CrewMember cm) {
        cm.heal(this.hpRestore);

    }
}
