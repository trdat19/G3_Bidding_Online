package server.model.item;

import shared.enums.ItemType;

public class Vehicle extends Item{
    private String manufacturer;
    private int year;

    public Vehicle(String name, String description, double basePrice, String sellerId, String manufacturer, int year) {
        super(name, description, basePrice, sellerId);
        this.manufacturer = manufacturer;
        this.year = year;
        type = ItemType.VEHICLE;
    }

    @Override
    public String getType() {
        return type.name();
    }
    public void printInfo() {
        System.out.println("Vehicle: " + name + "| Manufacturer: " + manufacturer + "| Year: " + year);
    }
}
