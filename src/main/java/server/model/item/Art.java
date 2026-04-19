package server.model.item;

import shared.enums.ItemType;

public class Art extends Item{
    private String artist;
    private String material;
    private int year;

    public Art(String name, String description, double basePrice, String sellerId, String artist, String material, int year) {
        super(name, description, basePrice, sellerId);
        this.artist = artist;
        this.material = material;
        this.year = year;
        type = ItemType.ART;
    }

    @Override
    public String getType() {
        return type.name();
    }
    public void printInfo() {
        System.out.println("Art: " + name + "| Artist: " + artist + "| Year: " + year);
    }
}
