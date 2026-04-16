package com.example.spacecolony;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Storage {
    private HashMap<Integer, CrewMember> crew;
    private HashMap<String, Integer> items; // Using String (class name) as key for easier management
    private ArrayList<Spaceship> spaceships;
    private static Storage instance;

    private Storage() {
        this.crew = new HashMap<>();
        this.items = new HashMap<>();
        this.spaceships = new ArrayList<>();
    }

    public static Storage getInstance() {
        if (instance == null) {
            instance = new Storage();
        }
        return instance;
    }

    public static void reset() {
        instance = null;
    }

    public boolean recruitCrewMember(CrewMember cm) {
        int priceToPay = GameManager.getInstance().getInflatedPrice(cm.getPrice());
        if (GameManager.getInstance().spendCredits(priceToPay)) {
            this.crew.put(cm.getId(), cm);
            if (!GameManager.getInstance().getAllCrew().contains(cm)) {
                GameManager.getInstance().getAllCrew().add(cm);
            }
            return true;
        }
        return false;
    }

    public void addLoadedCrewMember(CrewMember cm) {
        this.crew.put(cm.getId(), cm);
    }

    public void removeCrewMember(int id) {
        this.crew.remove(id);
    }

    public CrewMember getCrewMember(int id) {
        return this.crew.get(id);
    }

    public void addItem(String itemName) {
        this.items.put(itemName, this.items.getOrDefault(itemName, 0) + 1);
    }

    public Map<String, Integer> getItems() {
        return items;
    }

    public void buyItem(Item item) {
        int priceToPay = GameManager.getInstance().getInflatedPrice(item.getPrice());
        if (GameManager.getInstance().spendCredits(priceToPay)) {
            addItem(item.getClass().getSimpleName());
        }
    }

    public void removeItem(Item item) {
        String key = item.getClass().getSimpleName();
        int count = items.getOrDefault(key, 0);
        if (count > 0) {
            this.items.put(key, count - 1);
        }
    }

    public void buySpaceship(Spaceship spaceship) {
        int priceToPay = GameManager.getInstance().getInflatedPrice(spaceship.getPrice());
        if (GameManager.getInstance().spendCredits(priceToPay)) {
            this.spaceships.add(spaceship);
        }
    }

    public ArrayList<Spaceship> getSpaceships() {
        return spaceships;
    }

    public int getItemCount(String itemName) {
        return items.getOrDefault(itemName, 0);
    }

    public void upgradeBuilding(Building building, Builder builder) {
        int priceToPay = GameManager.getInstance().getInflatedPrice(building.getUpgradeCost());
        if (GameManager.getInstance().spendCredits(priceToPay)) {
            builder.upgrade(building);
            building.increaseUpgradeCost();
        }
    }
}
