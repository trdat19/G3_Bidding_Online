package server.model.item;

import server.model.Entity;

public abstract class Item extends Entity {
    protected String name;
    protected String description;
    protected double startPrice;
    protected double currentPrice;

    public Item(String id, String name, String description, double startPrice) {
        super();
        this.name = name;
        this.description = description;
        this.startPrice = startPrice;
    }

    public abstract String getType();

    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double price) {
        currentPrice = price;
    }

    public abstract void printInfo();
}
