package com.example.spacecolony;

public class Spaceship {
    private String name;
    private int price;
    public Spaceship () {
        this.name = "Standard Scout";
        this.price = 300;
    }
    public Spaceship (String name, int price) {
        this.name = name;
        this.price = price;
    }
    public String getName() {
        return name;
    }
    public int getPrice () {
        return this.price;
    }
}
