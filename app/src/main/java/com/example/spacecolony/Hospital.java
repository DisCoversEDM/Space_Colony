package com.example.spacecolony;

import java.util.ArrayList;

public class Hospital extends Building {
    private static Hospital instance;
    private Hospital () {
        super();
    }
    public static Hospital getInstance () {
        if (instance == null) {
            instance = new Hospital();
        }
        return instance;
    }

    public static void reset() {
        instance = null;
    }

    @Override
    public int getCapacity() {
        return this.capacity * 2;
    }
    
    public void heal (ArrayList<CrewMember> crew) {
        // Count available medics in the hospital (staff)
        int medicsAvailable = 0;
        for (CrewMember cm : crew) {
            if (cm instanceof Medic && cm.isAlive()) {
                if (((Medic) cm).isActingAsStaff()) {
                    medicsAvailable++;
                }
            }
        }
        
        int healsLeft = medicsAvailable;
        
        // Everyone in the hospital gains energy as they are in a safe environment
        for (CrewMember cm : crew) {
            cm.changeEnergy(1);
        }

        // Heal patients first.
        // Patients are: Non-medics OR Medics not acting as staff.
        for (CrewMember cm : crew) {
            boolean isPatient = !(cm instanceof Medic) || !((Medic) cm).isActingAsStaff();
            if (isPatient && cm.isAlive() && healsLeft > 0) {
                cm.heal(this.gain);
                healsLeft--;
            }
        }
        
        // If there are extra medics (staff), they can heal each other if there's more than 1 staff
        if (healsLeft > 0 && medicsAvailable > 1) {
            for (CrewMember cm : crew) {
                if (cm instanceof Medic && ((Medic) cm).isActingAsStaff() && cm.isAlive() && healsLeft > 0) {
                    cm.heal(this.gain);
                    healsLeft--;
                }
            }
        }
    }
}
