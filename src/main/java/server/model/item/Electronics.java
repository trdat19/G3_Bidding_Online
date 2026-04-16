package server.model.item;

public class Electronics extends Item{
    private String brand;
    private int warrantyMonths;

    public Electronics(String id, String name, String description, double startPrice, String brand, int warrantyMonths) {
        super(id, name, description, startPrice);
        this.brand = brand;
        this.warrantyMonths = warrantyMonths;
    }


    @Override
    public String getType() {
        return "ELECTRONICS";
    }
    public void printInfo() {
        //show sth;
        System.out.println("Electronics: " + name + "| Brand: " + brand + "| Price: " + currentPrice);
    }

}
