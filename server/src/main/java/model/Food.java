package model;

import java.io.Serializable;

public class Food implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String name;
    private double price;

    public Food(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return this.name;
    }

    public double getPrice() {
        return this.price;
    }

    public boolean equals(Food other) {
        return this.name.equals(other.name) && this.price == other.price;
    }
}