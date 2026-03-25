package com.example.spacecolony;

import java.util.ArrayList;
import java.util.HashMap;

public class Storage {
    private int money;
    private HashMap<Integer, CrewMember> crew;
    private HashMap<Item, Integer> items;
    private ArrayList<Spaceship> spaceships;
    private static Storage instance;
    private Storage() {
        this.money = 100;
        this.crew = new HashMap<>();
        this.items = new HashMap<>();
        this.spaceships = new ArrayList<>();
    }
    public static Storage getInstance () {
        if (instance == null) {
            instance = new Storage();
        }
        return instance;
    }
    public void recruitCrewMember (CrewMember cm) {
        if (cm.getPrice() <= this.money) {
            this.money -= cm.getPrice();
            this.crew.put(cm.getId(), cm);
        }
    }
    public void removeCrewMember (int id) {
        this.crew.remove(id);
    }
    public CrewMember getCrewMember (int id) {
        return this.crew.get(id);
    }
    public void buyItem (Item item) {
        if (item.getPrice() <= this.money) {
            this.money -= item.getPrice();
            this.items.put(item, this.items.getOrDefault(item, 0) + 1);
        }
    }
    public void removeItem (Item item) {
        this.items.put(item, this.items.get(item) - 1);
    }
    public void buySpaceship (Spaceship spaceship) {
        if (spaceship.getPrice() <= this.money) {
            this.money -= spaceship.getPrice();
            this.spaceships.add(spaceship);
        }
    }
    public void upgradeBuilding (Building building, Builder builder) {
        if (building.getUpgradeCost() <= this.money) {
            this.money -= building.getUpgradeCost();
            builder.upgrade(building);
            building.increaseUpgradeCost();
        }
    }
}
