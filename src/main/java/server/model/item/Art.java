package server.model.item;

public class Art extends Item{
    private String artist;
    private int year;

    public Art(String id, String name, String description, double startPrice, String artist, int year) {
        super(id, name, description, startPrice);
        this.artist = artist;
        this.year = year;
    }

    @Override
    public String getType() {
        return "ART";
    }
    public void printInfo() {
        System.out.println("Art: " + name + "| Artist: " + artist + "| Year: " + year);
    }
}
