package server.model;

public class Item {
    private String id, name;
    private double currentPrice;

    //constructor
    public Item(String id, String name, double currentPrice) {
        this.id = id;
        this.name = name;
        this.currentPrice = currentPrice;
    }

    //setter
    public void setName(String newName) {
        this.name = newName;
    }
    public void setCurrentPrice(double newCurrentPrice) {
        this.currentPrice = newCurrentPrice;
    }

    //getter
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public double getCurrentPrice() {
        return currentPrice;
    }

    //display
    public void display() {
        System.out.println("ID: " + id + " - Name: " + name + " - CurrentPrice: " + currentPrice);
    }
}
