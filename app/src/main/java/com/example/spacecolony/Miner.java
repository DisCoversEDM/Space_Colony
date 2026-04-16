package com.example.spacecolony;

public class Miner extends CrewMember {
    private int miningMinLand;
    private int miningMaxLand;
    private int lastPayout = 0;
    private boolean isMining = false;
    private boolean miningAsteroid = false;

    public Miner(String name) {
        super(name, 100, 6, 3, 8, 12);
        this.miningMinLand = 20;
        this.miningMaxLand = 40;
    }

    public int mineLand() {
        return (int) (Math.random() * (this.miningMaxLand - this.miningMinLand)) + this.miningMinLand;
    }

    public int mineAsteroid(Spaceship spaceship) {
        if (spaceship != null) {
            return mineLand() * 5;
        } else {
            return 0;
        }
    }

    public int getLastPayout() {
        return lastPayout;
    }

    public void setLastPayout(int lastPayout) {
        this.lastPayout = lastPayout;
    }

    public boolean isMining() {
        return isMining;
    }

    public void startMining(boolean asteroid) {
        if (this.energy > 0) {
            this.isMining = true;
            this.miningAsteroid = asteroid;
        }
    }

    public void stopMining() {
        this.isMining = false;
        this.miningAsteroid = false;
    }

    public int completeMining(Spaceship spaceship) {
        if (!isMining) return 0;

        if (this.energy <= 0) {
            stopMining();
            return 0;
        }
        
        int payout;
        if (miningAsteroid) {
            if (spaceship != null) {
                payout = mineAsteroid(spaceship);
            } else {
                // If they were mining asteroid but no ship is available this turn, they get land payout or 0?
                // The prompt says "results of mining", if ship is missing they might fail or just mine land.
                // Let's assume they need the ship for asteroid payout.
                payout = 0; 
            }
        } else {
            payout = mineLand();
        }
        
        this.lastPayout = payout;
        this.changeEnergy(-1);
        
        // Check if they ran out of energy after this turn
        if (this.energy <= 0) {
            this.isMining = false;
        }

        return payout;
    }
    
    public boolean isMiningAsteroid() {
        return miningAsteroid;
    }
}
