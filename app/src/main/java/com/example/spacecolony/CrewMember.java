package com.example.spacecolony;

public abstract class CrewMember {
    protected String name;
    protected int price;
    protected int experience;
    protected int attack;
    protected int defense;
    protected int energy;
    protected int maxEnergy;
    protected int healthPoints;
    protected int maxHP;
    protected int id;
    protected boolean alive;
    private static int idCounter = 0;

    public CrewMember (String name, int price, int attack, int defense, int maxEnergy, int maxHP) {
        this.name = name;
        this.price = price;
        this.attack = attack;
        this.defense = defense;
        this.energy = maxEnergy;
        this.maxEnergy = maxEnergy;
        this.healthPoints = maxHP;
        this.maxHP = maxHP;
        this.id = idCounter;
        this.alive = true; // Initialize as alive
        idCounter++;
    }

    public void attack (Threat target) {
        if (this.energy > 0) {
            target.takeDamage(this.attack);
            this.energy -= 1;
        }
    }
    public void takeDamage (int points) {
        if (points > this.defense) {
            this.healthPoints -= (points-this.defense);
        }
        if (this.healthPoints <= 0) {
            this.alive = false;
            this.healthPoints = 0;
            GameManager.getInstance().handleCrewDeath(this);
        }
    }
    public void changeEnergy (int points) {
        if (points + this.energy >= this.maxEnergy) {
            this.energy = this.maxEnergy;
        }
        else if (points + this.energy <= 0) {
            this.energy = 0;
        }
        else {
            this.energy += points;
        }
    }
    public void increaseXP (int points) {
        this.experience += points;
        this.attack += points;
        this.defense += points;
    }
    public void heal (int points) {
        if (points + this.healthPoints >= this.maxHP) {
            this.healthPoints = this.maxHP;
        }
        else {
            this.healthPoints += points;
        }
    }
    public void use (Item item) {
        item.used(this);
        Storage.getInstance().removeItem(item);
    }
    public static int getNumberOfCreated () {
        return idCounter;
    }
    public static void setIdCounter(int value) {
        idCounter = value;
    }
    public int getPrice () {
        return this.price;
    }
    public int getId () {
        return this.id;
    }
    public boolean isAlive () {
        return this.alive;
    }
}
