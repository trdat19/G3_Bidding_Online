package server.model.item;

import shared.enums.ItemCategory;
import shared.enums.ItemStatus;
import java.math.BigDecimal;

public class Art extends Item {

    private static final long serialVersionUID = 1L;

    private String artist;
    private int year;
    private String material;

    public Art() {}

    public Art(String nameItem, String description, Long sellerId,
               ItemStatus statusItem) {
        super(nameItem, description, sellerId, ItemCategory.ART, statusItem);
        //set thuoc tinh rieng
    }

    //getter
    public String getArtist() { return artist; }
    public int getYear() { return year; }
    public String getMaterial() { return material; }

    //setter
    public void setArtist(String artist) { this.artist = artist; }
    public void setYear(int year) { this.year = year; }
    public void setMaterial(String material) { this.material = material; }

    @Override
    public String getInfo() {
        return String.format("Art: %s | Artist: %s | Year: %d | Material: %s ",
                nameItem, artist, year, material);
    }
}