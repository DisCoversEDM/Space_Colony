package com.example.spacecolony;

public class FirstAidKit extends Item {
    public FirstAidKit () {
        super(60);
    }
    public void used (CrewMember cm) {
        cm.heal(1000000);
    }
}
