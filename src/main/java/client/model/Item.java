package client.model;

public class Item {
    private String category;
    private String title;
    private String description;
    private String price;
    private String status;

    public Item(String category, String title, String description, String price, String status) {
        this.category = category;
        this.title = title;
        this.description = description;
        this.price = price;
        this.status = status;
    }
    public String getCategory() {
        return category;
    }
    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }
    public String getPrice() {
        return price;
    }
    public String getStatus() {
        return status;
    }
}
