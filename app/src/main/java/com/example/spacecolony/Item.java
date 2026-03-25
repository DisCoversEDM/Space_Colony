package com.example.spacecolony;

public abstract class Item {
    private int price;
    public Item (int price) {
        this.price = price;
    }
    public int getPrice () {
        return this.price;
    }
    abstract public void used (CrewMember cm);
}
