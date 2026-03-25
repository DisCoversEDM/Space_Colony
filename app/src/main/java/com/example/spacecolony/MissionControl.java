package com.example.spacecolony;

import java.util.ArrayList;

public class MissionControl {
    private int turnsUntilNextThreat;
    private int missionsCompleted;
    public static MissionControl instance;
    private MissionControl () {
        this.turnsUntilNextThreat = 5;
        this.missionsCompleted = 0;
    }
    public static MissionControl getInstance () {
        if (instance == null) {
            instance = new MissionControl();
        }
        return instance;
    }
    public void startMission (ArrayList<CrewMember> crew) {
        if (crew.size() >= 1 && crew.size() <= 3) {
            Threat threat = new Threat(4+this.missionsCompleted, 1+this.missionsCompleted,(double) this.missionsCompleted / 100, 15 + (this.missionsCompleted / 5), 8+(this.missionsCompleted*2));
            while (threat.isAlive() && crew.stream().anyMatch(CrewMember::isAlive)) {

            }
        }
    }
    private void turn (CrewMember cm, Threat threat, String action, Item item) {
        if (action.equals("attack")) {
            cm.attack(threat);
        }
        else if (action.equals("use")) {
            cm.use(item);
        }
        else if (action.equals("nothing")) {
            cm.changeEnergy(1);
        }
    }
}
