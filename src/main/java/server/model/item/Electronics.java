package server.model.item;

import shared.enums.ItemType;

public class Electronics extends Item{
    private String brand;
    private int warrantyMonths;
    private String model;

    public Electronics(String name, String description, double basePrice, String sellerId, String brand, int warrantyMonths, String model) {
        super(name, description, basePrice, sellerId);
        this.brand = brand;
        this.warrantyMonths = warrantyMonths;
        this.model = model;
        type = ItemType.ELECTRONICS;
    }


    @Override
    public String getType() {
        return type.name();
    }
    public void printInfo() {
        //show sth;
        System.out.println("Electronics: " + name + "| Brand: " + brand + "| Price: ");
    }

}
