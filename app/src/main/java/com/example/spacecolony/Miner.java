package com.example.spacecolony;

public class Miner extends CrewMember{
    private int miningMinLand;
    private int miningMaxLand;
    public Miner (String name) {
        super(name, 100, 6, 3, 8, 12);
        this.miningMinLand = 20;
        this.miningMaxLand = 40;
    }
    public int mineLand () {
        return (int) (Math.random() * (this.miningMaxLand - this.miningMinLand)) + this.miningMinLand;
    }
    public int mineAstroid (Spaceship spaceship) {
        if (spaceship != null) {
            return mineLand() * 5;
        }
        else {
            return 0;
        }
    }
}
