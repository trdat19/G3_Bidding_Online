package server.model.item;

public class Vehicle extends Item{
    private String manufacturer;
    private int year;

    public Vehicle(String id, String name, String description, double startPrice, String manufacturer, int year) {
        super(id, name, description, startPrice);
        this.manufacturer = manufacturer;
        this.year = year;
    }

    @Override
    public String getType() {
        return "VEHICLE";
    }
    public void printInfo() {
        System.out.println("Vehicle: " + name + "| Manufacturer: " + manufacturer + "| Year: " + year);
    }
}
