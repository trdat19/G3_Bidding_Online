package server.model.item;

import shared.enums.ItemCategory;
import shared.enums.ItemStatus;

public class Vehicle extends Item {

    private static final long serialVersionUID = 1L;

    private String manufacturer;
    private int year;

    public Vehicle() {}

    public Vehicle(String nameItem, String description, Long sellerId,
                   ItemStatus statusItem) {
        super(nameItem, description, sellerId, ItemCategory.VEHICLE, statusItem);
        //set thuoc tinh rieng
    }

    //getter
    public String getManufacturer() { return manufacturer; }
    public int getYear() { return year; }

    //setter
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    public void setYear(int year) { this.year = year; }

    @Override
    public String getInfo() {
        return String.format("Vehicle: %s | Manufacturer: %s | Year: %d",
                nameItem, manufacturer, year);
    }
}
