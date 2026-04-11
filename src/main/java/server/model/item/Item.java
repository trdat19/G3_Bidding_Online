package server.model.item;

import server.model.Entity;

public abstract class Item extends Entity {
    protected String name;
    protected String description;
    protected double startPrice;

    public Item(String id, String name, String description, double startPrice) {
        super();
        this.name = name;
        this.description = description;
        this.startPrice = startPrice;
    }

    public abstract String getType();

    public String getName() {
        return name;
    }
}
