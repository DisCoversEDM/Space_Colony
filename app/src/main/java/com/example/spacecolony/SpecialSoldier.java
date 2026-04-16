package com.example.spacecolony;

public class SpecialSoldier extends CrewMember {
    private double critChance;
    private int critAttackBoost;
    public SpecialSoldier (String name) {
        super(name, 200, 15, 5, 25, 30);
        this.critChance = 0.05;
        this.critAttackBoost = 15;
    }
    @Override
    public void attack (Threat target) {
        if (this.energy > 0) {
            if (Math.random() < this.critChance) {
                target.takeDamage(this.attack + this.critAttackBoost);
            }
            else {
                target.takeDamage(this.attack);
            }
            this.energy -= 1;
        }
    }
}
