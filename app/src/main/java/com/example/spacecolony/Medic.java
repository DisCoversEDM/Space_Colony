package com.example.spacecolony;

public class Medic extends CrewMember {
    private boolean actingAsStaff = true;

    public Medic (String name) {
        super(name, 90, 3, 1, 5, 7);
    }

    public boolean isActingAsStaff() {
        return actingAsStaff;
    }

    public void setActingAsStaff(boolean actingAsStaff) {
        this.actingAsStaff = actingAsStaff;
    }
}
