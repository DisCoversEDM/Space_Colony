package com.example.spacecolony;

public class Threat {
    private int attack;
    private int defense;
    private double critChance;
    private int critAttackBoost;
    private int healthPoints;
    private boolean alive;
    public Threat (int attack, int defense, double critChance, int critAttackBoost, int maxHP) {
        this.attack = attack;
        this.defense = defense;
        this.critChance = critChance;
        this.critAttackBoost = critAttackBoost;
        this.healthPoints = maxHP;
        this.alive = true;
    }
    public void attack (CrewMember target) {
        if (Math.random() < this.critChance) {
            target.takeDamage(this.attack + this.critAttackBoost);
        }
        else {
            target.takeDamage(this.attack);
        }
    }
    public void takeDamage (int points) {
        if (points > this.defense) {
            this.healthPoints -= (points-this.defense);
        }
        if (this.healthPoints <= 0) {
            this.alive = false;
        }
    }
    public boolean isAlive () {
        return this.alive;
    }
    public int getHealthPoints() {
        return this.healthPoints;
    }
}
